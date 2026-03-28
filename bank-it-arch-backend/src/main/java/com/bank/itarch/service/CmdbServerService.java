package com.bank.itarch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.mapper.CmdbServerMapper;
import com.bank.itarch.model.dto.CmdbServerDTO;
import com.bank.itarch.model.entity.CmdbAssetChangeLog;
import com.bank.itarch.model.entity.CmdbServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmdbServerService extends ServiceImpl<CmdbServerMapper, CmdbServer> {

    private final CmdbAssetChangeLogService changeLogService;
    private final ObjectMapper objectMapper;

    public PageResult<CmdbServerDTO> pageQuery(PageQuery query, String keyword, String status, Long departmentId) {
        Page<CmdbServer> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<CmdbServer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), CmdbServer::getHostname, keyword)
               .or(StringUtils.hasText(keyword))
               .like(StringUtils.hasText(keyword), CmdbServer::getIpAddress, keyword)
               .eq(StringUtils.hasText(status), CmdbServer::getStatus, status)
               .eq(departmentId != null, CmdbServer::getDepartmentId, departmentId)
               .orderByDesc(CmdbServer::getCreateTime);

        IPage<CmdbServer> result = page(page, wrapper);

        List<CmdbServerDTO> dtoList = result.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    public CmdbServerDTO getById(Long id) {
        CmdbServer server = super.getById(id);
        if (server == null) {
            throw new BusinessException(2001, "Server not found");
        }
        return toDTO(server);
    }

    @Transactional
    public CmdbServerDTO create(CmdbServerDTO dto) {
        CmdbServer server = toEntity(dto);
        server.setAssetCode(generateAssetCode());
        save(server);
        saveChangeLog(server.getId(), "CREATE", null, server);
        return toDTO(server);
    }

    @Transactional
    public CmdbServerDTO update(Long id, CmdbServerDTO dto) {
        CmdbServer existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Server not found");
        }
        CmdbServer before = copyEntity(existing);
        updateEntity(existing, dto);
        updateById(existing);
        saveChangeLog(id, "UPDATE", before, existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        CmdbServer existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(2001, "Server not found");
        }
        saveChangeLog(id, "DELETE", existing, null);
        removeById(id);
    }

    private String generateAssetCode() {
        long count = count() + 1;
        return String.format("ASSET-SERVER-%05d", count);
    }

    private void saveChangeLog(Long assetId, String operateType, CmdbServer before, CmdbServer after) {
        try {
            CmdbAssetChangeLog log = new CmdbAssetChangeLog();
            log.setAssetType("server");
            log.setAssetId(assetId);
            log.setAssetCode(before != null ? before.getAssetCode() : after.getAssetCode());
            log.setOperateType(operateType);
            log.setBeforeValue(before != null ? objectMapper.writeValueAsString(toDTO(before)) : null);
            log.setAfterValue(after != null ? objectMapper.writeValueAsString(toDTO(after)) : null);
            changeLogService.save(log);
        } catch (Exception e) {
            // Log error but don't fail the transaction
        }
    }

    private CmdbServerDTO toDTO(CmdbServer server) {
        CmdbServerDTO dto = new CmdbServerDTO();
        dto.setId(server.getId());
        dto.setAssetCode(server.getAssetCode());
        dto.setHostname(server.getHostname());
        dto.setIpAddress(server.getIpAddress());
        dto.setInnerIp(server.getInnerIp());
        dto.setCpu(server.getCpu());
        dto.setCpuCount(server.getCpuCount());
        dto.setMemory(server.getMemory());
        dto.setMemorySize(server.getMemorySize());
        dto.setDisk(server.getDisk());
        dto.setDiskSize(server.getDiskSize());
        dto.setOs(server.getOs());
        dto.setOsVersion(server.getOsVersion());
        dto.setServerType(server.getServerType());
        dto.setStatus(server.getStatus());
        dto.setDepartmentId(server.getDepartmentId());
        dto.setDepartmentName(server.getDepartmentName());
        dto.setTeamId(server.getTeamId());
        dto.setTeamName(server.getTeamName());
        dto.setIdc(server.getIdc());
        dto.setCabinet(server.getCabinet());
        dto.setManufacturer(server.getManufacturer());
        dto.setModel(server.getModel());
        dto.setSerialNumber(server.getSerialNumber());
        dto.setPurchaseDate(server.getPurchaseDate());
        dto.setWarrantyExpire(server.getWarrantyExpire());
        dto.setRemark(server.getRemark());
        return dto;
    }

    private CmdbServer toEntity(CmdbServerDTO dto) {
        CmdbServer server = new CmdbServer();
        server.setHostname(dto.getHostname());
        server.setIpAddress(dto.getIpAddress());
        server.setInnerIp(dto.getInnerIp());
        server.setCpu(dto.getCpu());
        server.setCpuCount(dto.getCpuCount());
        server.setMemory(dto.getMemory());
        server.setMemorySize(dto.getMemorySize());
        server.setDisk(dto.getDisk());
        server.setDiskSize(dto.getDiskSize());
        server.setOs(dto.getOs());
        server.setOsVersion(dto.getOsVersion());
        server.setServerType(dto.getServerType());
        server.setStatus(dto.getStatus());
        server.setDepartmentId(dto.getDepartmentId());
        server.setDepartmentName(dto.getDepartmentName());
        server.setTeamId(dto.getTeamId());
        server.setTeamName(dto.getTeamName());
        server.setIdc(dto.getIdc());
        server.setCabinet(dto.getCabinet());
        server.setManufacturer(dto.getManufacturer());
        server.setModel(dto.getModel());
        server.setSerialNumber(dto.getSerialNumber());
        server.setPurchaseDate(dto.getPurchaseDate());
        server.setWarrantyExpire(dto.getWarrantyExpire());
        server.setRemark(dto.getRemark());
        return server;
    }

    private void updateEntity(CmdbServer server, CmdbServerDTO dto) {
        if (dto.getHostname() != null) server.setHostname(dto.getHostname());
        if (dto.getIpAddress() != null) server.setIpAddress(dto.getIpAddress());
        if (dto.getInnerIp() != null) server.setInnerIp(dto.getInnerIp());
        if (dto.getCpu() != null) server.setCpu(dto.getCpu());
        if (dto.getCpuCount() != null) server.setCpuCount(dto.getCpuCount());
        if (dto.getMemory() != null) server.setMemory(dto.getMemory());
        if (dto.getMemorySize() != null) server.setMemorySize(dto.getMemorySize());
        if (dto.getDisk() != null) server.setDisk(dto.getDisk());
        if (dto.getDiskSize() != null) server.setDiskSize(dto.getDiskSize());
        if (dto.getOs() != null) server.setOs(dto.getOs());
        if (dto.getOsVersion() != null) server.setOsVersion(dto.getOsVersion());
        if (dto.getServerType() != null) server.setServerType(dto.getServerType());
        if (dto.getStatus() != null) server.setStatus(dto.getStatus());
        if (dto.getDepartmentId() != null) server.setDepartmentId(dto.getDepartmentId());
        if (dto.getDepartmentName() != null) server.setDepartmentName(dto.getDepartmentName());
        if (dto.getTeamId() != null) server.setTeamId(dto.getTeamId());
        if (dto.getTeamName() != null) server.setTeamName(dto.getTeamName());
        if (dto.getIdc() != null) server.setIdc(dto.getIdc());
        if (dto.getCabinet() != null) server.setCabinet(dto.getCabinet());
        if (dto.getManufacturer() != null) server.setManufacturer(dto.getManufacturer());
        if (dto.getModel() != null) server.setModel(dto.getModel());
        if (dto.getSerialNumber() != null) server.setSerialNumber(dto.getSerialNumber());
        if (dto.getPurchaseDate() != null) server.setPurchaseDate(dto.getPurchaseDate());
        if (dto.getWarrantyExpire() != null) server.setWarrantyExpire(dto.getWarrantyExpire());
        if (dto.getRemark() != null) server.setRemark(dto.getRemark());
    }

    private CmdbServer copyEntity(CmdbServer source) {
        CmdbServer copy = new CmdbServer();
        copy.setId(source.getId());
        copy.setAssetCode(source.getAssetCode());
        copy.setHostname(source.getHostname());
        copy.setIpAddress(source.getIpAddress());
        copy.setInnerIp(source.getInnerIp());
        copy.setCpu(source.getCpu());
        copy.setCpuCount(source.getCpuCount());
        copy.setMemory(source.getMemory());
        copy.setMemorySize(source.getMemorySize());
        copy.setDisk(source.getDisk());
        copy.setDiskSize(source.getDiskSize());
        copy.setOs(source.getOs());
        copy.setOsVersion(source.getOsVersion());
        copy.setServerType(source.getServerType());
        copy.setStatus(source.getStatus());
        copy.setDepartmentId(source.getDepartmentId());
        copy.setDepartmentName(source.getDepartmentName());
        copy.setTeamId(source.getTeamId());
        copy.setTeamName(source.getTeamName());
        copy.setIdc(source.getIdc());
        copy.setCabinet(source.getCabinet());
        copy.setManufacturer(source.getManufacturer());
        copy.setModel(source.getModel());
        copy.setSerialNumber(source.getSerialNumber());
        copy.setPurchaseDate(source.getPurchaseDate());
        copy.setWarrantyExpire(source.getWarrantyExpire());
        copy.setRemark(source.getRemark());
        return copy;
    }
}
