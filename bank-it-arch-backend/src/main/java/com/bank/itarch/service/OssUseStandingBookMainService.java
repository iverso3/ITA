package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.OssUseStandingBookMainMapper;
import com.bank.itarch.mapper.OssUseStandingBookDetailsMapper;
import com.bank.itarch.model.dto.ImportResult;
import com.bank.itarch.model.dto.OssUseStandingBookImportDTO;
import com.bank.itarch.model.dto.OssUseStandingBookMainDTO;
import com.bank.itarch.model.dto.OssUseStandingBookPageQuery;
import com.bank.itarch.model.entity.OssUseStandingBookMain;
import com.bank.itarch.model.entity.OssUseStandingBookDetails;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OssUseStandingBookMainService extends ServiceImpl<OssUseStandingBookMainMapper, OssUseStandingBookMain> {

    private final OssUseStandingBookDetailsMapper detailsMapper;

    public PageResult<OssUseStandingBookMainDTO> pageQuery(OssUseStandingBookPageQuery query) {
        Page<OssUseStandingBookMain> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .like(StringUtils.hasText(query.getSwFullName()), OssUseStandingBookMain::getSwFullName, query.getSwFullName())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        IPage<OssUseStandingBookMain> result = page(page, wrapper);

        List<OssUseStandingBookMainDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public OssUseStandingBookMainDTO getById(String id) {
        OssUseStandingBookMain main = super.getById(id);
        if (main == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO create(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = toEntity(dto);
        main.setCreateMode(0);
        save(main);
        return toDTO(main);
    }

    @Transactional
    public OssUseStandingBookMainDTO update(String id, OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        updateEntity(existing, dto);
        updateById(existing);

        // 同步更新关联的Detail冗余字段
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            syncDetailFromMain(detail, existing);
            detailsMapper.updateById(detail);
        }

        return toDTO(existing);
    }

    @Transactional
    public void delete(String id) {
        OssUseStandingBookMain existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Main record not found");
        }
        // 逻辑删除关联的Detail
        LambdaQueryWrapper<OssUseStandingBookDetails> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OssUseStandingBookDetails::getParentId, id);
        List<OssUseStandingBookDetails> details = detailsMapper.selectList(wrapper);
        for (OssUseStandingBookDetails detail : details) {
            detail.setLogicStatus(1);
            detailsMapper.updateById(detail);
        }
        removeById(id);
    }

    public List<OssUseStandingBookMainDTO> listAllForExport(OssUseStandingBookPageQuery query) {
        LambdaQueryWrapper<OssUseStandingBookMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSwName()), OssUseStandingBookMain::getSwName, query.getSwName())
               .like(StringUtils.hasText(query.getSwVersion()), OssUseStandingBookMain::getSwVersion, query.getSwVersion())
               .eq(StringUtils.hasText(query.getSwCategory()), OssUseStandingBookMain::getSwCategory, query.getSwCategory())
               .like(StringUtils.hasText(query.getAppName()), OssUseStandingBookMain::getAppName, query.getAppName())
               .eq(StringUtils.hasText(query.getEnvironment()), OssUseStandingBookMain::getEnvironment, query.getEnvironment())
               .orderByDesc(OssUseStandingBookMain::getCreateDatetime);

        List<OssUseStandingBookMain> list = list(wrapper);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ImportResult importExcel(MultipartFile file) {
        // 1. 解析 Excel
        List<OssUseStandingBookImportDTO> rows;
        try {
            rows = parseExcel(file);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
        }

        // 2. 校验空行
        List<ImportResult.ImportError> errors = new ArrayList<>();
        List<OssUseStandingBookImportDTO> validRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            OssUseStandingBookImportDTO row = rows.get(i);
            if (!StringUtils.hasText(row.getSwName()) || !StringUtils.hasText(row.getSwVersion()) || !StringUtils.hasText(row.getAppNo())) {
                errors.add(new ImportResult.ImportError(i + 2, "swName/swVersion/appNo 不能为空"));
            } else {
                validRows.add(row);
            }
        }

        // 3. 按 (swName, swVersion, appNo) 分组
        Map<String, List<OssUseStandingBookImportDTO>> groupMap = validRows.stream()
                .collect(Collectors.groupingBy(r -> r.getSwName() + "|" + r.getSwVersion() + "|" + r.getAppNo()));

        // 4. 先删后写
        for (Map.Entry<String, List<OssUseStandingBookImportDTO>> entry : groupMap.entrySet()) {
            List<OssUseStandingBookImportDTO> groupRows = entry.getValue();
            OssUseStandingBookImportDTO firstRow = groupRows.get(0);
            String swName = firstRow.getSwName();
            String swVersion = firstRow.getSwVersion();
            String appNo = firstRow.getAppNo();

            // 硬删除 detail 表
            detailsMapper.hardDeleteBySwKey(swName, swVersion, appNo);
            // 硬删除 main 表
            LambdaQueryWrapper<OssUseStandingBookMain> mainWrapper = new LambdaQueryWrapper<>();
            mainWrapper.eq(OssUseStandingBookMain::getSwName, swName)
                       .eq(OssUseStandingBookMain::getSwVersion, swVersion)
                       .eq(OssUseStandingBookMain::getAppNo, appNo);
            baseMapper.delete(mainWrapper);

            // 创建 main 记录
            OssUseStandingBookMain main = new OssUseStandingBookMain();
            main.setId(UUID.randomUUID().toString());
            main.setSwName(swName);
            main.setSwVersion(swVersion);
            main.setSwFullName(firstRow.getSwFullName());
            main.setLicAbbr(firstRow.getLicAbbr());
            main.setEnvironment(firstRow.getEnvironment());
            main.setSwCategory(firstRow.getSwCategory());
            main.setAppNo(appNo);
            main.setAppName(firstRow.getAppName());
            main.setScanDate(LocalDate.now());
            main.setSyncDatetime(LocalDateTime.now());
            main.setCreateMode(0);
            main.setLogicStatus(0);
            main.setIsCommerc(firstRow.getIsCommerc());
            baseMapper.insert(main);

            // 创建 detail 记录（每行一条）
            for (OssUseStandingBookImportDTO row : groupRows) {
                OssUseStandingBookDetails detail = new OssUseStandingBookDetails();
                detail.setId(UUID.randomUUID().toString().replace("-", ""));
                detail.setParentId(main.getId());
                detail.setSwName(swName);
                detail.setSwVersion(swVersion);
                detail.setSwFullName(row.getSwFullName());
                detail.setLicAbbr(row.getLicAbbr());
                detail.setEnvironment(row.getEnvironment());
                detail.setSwCategory(row.getSwCategory());
                detail.setAppNo(appNo);
                detail.setAppName(row.getAppName());
                detail.setInstallPath(row.getInstallPath());
                detail.setSource(row.getSource() != null ? row.getSource() : 1);
                detail.setScanTime(row.getScanTime());
                detail.setIpOrHostName(row.getIpOrHostName());
                detail.setCommand(row.getCommand());
                detail.setDetailedInfo(row.getDetailedInfo());
                detail.setFileType(row.getFileType());
                detail.setDependType(row.getDependType());
                detail.setSyncDatetime(LocalDateTime.now());
                detail.setCreateMode(0);
                detail.setLogicStatus(0);
                detail.setIsCommerc(row.getIsCommerc());
                detail.setCommercProductName(row.getCommercProductName());
                detail.setCommercProductVersion(row.getCommercProductVersion());
                detail.setProjectName(row.getProjectName());
                detailsMapper.insert(detail);
            }
        }

        if (!errors.isEmpty()) {
            return ImportResult.error(rows.size(), errors);
        }
        return ImportResult.ok(rows.size(), validRows.size());
    }

    private List<OssUseStandingBookImportDTO> parseExcel(MultipartFile file) throws Exception {
        List<OssUseStandingBookImportDTO> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(400, "Excel表头不能为空");
            }
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    headerMap.put(cell.getStringCellValue().trim(), i);
                }
            }
            String[] requiredHeaders = {"开源软件名称", "开源软件版本", "应用编号"};
            for (String h : requiredHeaders) {
                if (!headerMap.containsKey(h)) {
                    throw new BusinessException(400, "缺少必需列: " + h);
                }
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                OssUseStandingBookImportDTO dto = new OssUseStandingBookImportDTO();
                dto.setSwName(getCellValue(row, headerMap.get("开源软件名称")));
                dto.setSwVersion(getCellValue(row, headerMap.get("开源软件版本")));
                dto.setSwFullName(getCellValue(row, headerMap.get("开源软件全称")));
                dto.setLicAbbr(getCellValue(row, headerMap.get("开源许可证集合")));
                dto.setEnvironment(getCellValue(row, headerMap.get("所属环境")));
                dto.setSwCategory(getCellValue(row, headerMap.get("软件分类")));
                dto.setAppNo(getCellValue(row, headerMap.get("应用编号")));
                dto.setAppName(getCellValue(row, headerMap.get("应用全称")));
                dto.setInstallPath(getCellValue(row, headerMap.get("项目路径/安装路径")));
                // source
                Integer source = null;
                if (headerMap.containsKey("台账来源")) {
                    Cell cell = row.getCell(headerMap.get("台账来源"));
                    if (cell != null) {
                        try { source = (int) cell.getNumericCellValue(); } catch (Exception e) { /* ignore */ }
                    }
                }
                dto.setSource(source);
                // scanTime
                LocalDateTime scanTime = null;
                if (headerMap.containsKey("扫描时间")) {
                    Cell cell = row.getCell(headerMap.get("扫描时间"));
                    if (cell != null) {
                        try {
                            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                                scanTime = cell.getLocalDateTimeCellValue();
                            } else {
                                String val = getCellValue(row, headerMap.get("扫描时间"));
                                if (StringUtils.hasText(val)) {
                                    scanTime = LocalDateTime.parse(val, dtf);
                                }
                            }
                        } catch (Exception e) { /* ignore */ }
                    }
                }
                dto.setScanTime(scanTime);
                dto.setIpOrHostName(getCellValue(row, headerMap.get("IP/主机名称")));
                dto.setCommand(getCellValue(row, headerMap.get("软件启动命令")));
                dto.setDetailedInfo(getCellValue(row, headerMap.get("版本详细信息")));
                dto.setFileType(getCellValue(row, headerMap.get("组件文件类型")));
                dto.setDependType(getCellValue(row, headerMap.get("组件依赖类型")));
                dto.setIsCommerc(getCellValue(row, headerMap.get("是否商用")));
                dto.setCommercProductName(getCellValue(row, headerMap.get("产品名称")));
                dto.setCommercProductVersion(getCellValue(row, headerMap.get("产品版本")));
                dto.setProjectName(getCellValue(row, headerMap.get("项目名")));
                rows.add(dto);
            }
        }
        return rows;
    }

    private String getCellValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return null;
        }
    }

    private void syncDetailFromMain(OssUseStandingBookDetails detail, OssUseStandingBookMain main) {
        detail.setSwName(main.getSwName());
        detail.setSwVersion(main.getSwVersion());
        detail.setSwFullName(main.getSwFullName());
        detail.setLicAbbr(main.getLicAbbr());
        detail.setEnvironment(main.getEnvironment());
        detail.setSwCategory(main.getSwCategory());
        detail.setAppNo(main.getAppNo());
        detail.setAppName(main.getAppName());
        detail.setIsCommerc(main.getIsCommerc());
    }

    private OssUseStandingBookMainDTO toDTO(OssUseStandingBookMain main) {
        OssUseStandingBookMainDTO dto = new OssUseStandingBookMainDTO();
        dto.setId(main.getId());
        dto.setSwName(main.getSwName());
        dto.setSwVersion(main.getSwVersion());
        dto.setSwFullName(main.getSwFullName());
        dto.setLicAbbr(main.getLicAbbr());
        dto.setEnvironment(main.getEnvironment());
        dto.setScanDate(main.getScanDate());
        dto.setSwCategory(main.getSwCategory());
        dto.setAppNo(main.getAppNo());
        dto.setAppName(main.getAppName());
        dto.setSyncDatetime(main.getSyncDatetime());
        dto.setCreateMode(main.getCreateMode());
        dto.setCreateUserId(main.getCreateUserId());
        dto.setCreateDatetime(main.getCreateDatetime());
        dto.setUpdateUserId(main.getUpdateUserId());
        dto.setUpdateDatetime(main.getUpdateDatetime());
        dto.setIsCommerc(main.getIsCommerc());
        return dto;
    }

    private OssUseStandingBookMain toEntity(OssUseStandingBookMainDTO dto) {
        OssUseStandingBookMain main = new OssUseStandingBookMain();
        main.setSwName(dto.getSwName());
        main.setSwVersion(dto.getSwVersion());
        main.setSwFullName(dto.getSwFullName());
        main.setLicAbbr(dto.getLicAbbr());
        main.setEnvironment(dto.getEnvironment());
        main.setScanDate(dto.getScanDate());
        main.setSwCategory(dto.getSwCategory());
        main.setAppNo(dto.getAppNo());
        main.setAppName(dto.getAppName());
        main.setSyncDatetime(dto.getSyncDatetime());
        main.setCreateMode(dto.getCreateMode());
        main.setCreateUserId(dto.getCreateUserId());
        main.setUpdateUserId(dto.getUpdateUserId());
        main.setIsCommerc(dto.getIsCommerc());
        return main;
    }

    private void updateEntity(OssUseStandingBookMain main, OssUseStandingBookMainDTO dto) {
        if (dto.getSwName() != null) main.setSwName(dto.getSwName());
        if (dto.getSwVersion() != null) main.setSwVersion(dto.getSwVersion());
        if (dto.getSwFullName() != null) main.setSwFullName(dto.getSwFullName());
        if (dto.getLicAbbr() != null) main.setLicAbbr(dto.getLicAbbr());
        if (dto.getEnvironment() != null) main.setEnvironment(dto.getEnvironment());
        if (dto.getScanDate() != null) main.setScanDate(dto.getScanDate());
        if (dto.getSwCategory() != null) main.setSwCategory(dto.getSwCategory());
        if (dto.getAppNo() != null) main.setAppNo(dto.getAppNo());
        if (dto.getAppName() != null) main.setAppName(dto.getAppName());
        if (dto.getSyncDatetime() != null) main.setSyncDatetime(dto.getSyncDatetime());
        if (dto.getUpdateUserId() != null) main.setUpdateUserId(dto.getUpdateUserId());
        if (dto.getIsCommerc() != null) main.setIsCommerc(dto.getIsCommerc());
    }
}