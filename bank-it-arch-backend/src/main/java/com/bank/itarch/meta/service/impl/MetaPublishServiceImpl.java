package com.bank.itarch.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.constant.MetaStatus;
import com.bank.itarch.meta.mapper.*;
import com.bank.itarch.meta.model.dto.MetaModelVersionDTO;
import com.bank.itarch.meta.model.dto.MetaPublishRecordDTO;
import com.bank.itarch.meta.model.entity.*;
import com.bank.itarch.meta.service.MetaAuditService;
import com.bank.itarch.meta.service.MetaPublishService;
import com.bank.itarch.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 发布管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaPublishServiceImpl implements MetaPublishService {

    private final MetaModelVersionMapper versionMapper;
    private final MetaPublishRecordMapper recordMapper;
    private final MetaModelMapper modelMapper;
    private final MetaAuditService metaAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaModelVersion submitPublish(Long modelId) {
        MetaModel model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException(404, "模型不存在");
        }

        // 创建新版本
        int newVersion = model.getVersion() + 1;
        MetaModelVersion version = new MetaModelVersion();
        version.setModelId(modelId);
        version.setVersion(newVersion);
        version.setVersionName("v" + newVersion + ".0");
        version.setStatus(MetaStatus.VERSION_DRAFT);
        version.setCreator(UserContext.getUser().getUsername());

        versionMapper.insert(version);

        // 更新模型的当前版本号
        model.setVersion(newVersion);
        model.setStatus(MetaStatus.MODEL_PUBLISHED);
        modelMapper.updateById(model);

        log.info("提交发布成功: modelId={}, version={}", modelId, newVersion);
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePublish(Long versionId, String approveComment) {
        MetaModelVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(404, "版本不存在");
        }

        if (!MetaStatus.VERSION_DRAFT.equals(version.getStatus())) {
            throw new BusinessException(400, "只有草稿状态可以审核");
        }

        version.setStatus(MetaStatus.VERSION_TESTING);
        versionMapper.updateById(version);

        log.info("审核通过: versionId={}", versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaPublishRecordDTO executePublish(Long versionId) {
        MetaModelVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(404, "版本不存在");
        }

        MetaModel model = modelMapper.selectById(version.getModelId());
        if (model == null) {
            throw new BusinessException(404, "模型不存在");
        }

        // 创建发布记录
        MetaPublishRecord record = new MetaPublishRecord();
        record.setRecordCode("PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setModelId(model.getId());
        record.setVersionTo(version.getVersion());
        record.setActionType(MetaStatus.ACTION_PUBLISH);
        record.setStatus(MetaStatus.RECORD_PROCESSING);
        record.setOperator(UserContext.getUser().getUsername());
        record.setOperateTime(LocalDateTime.now());

        recordMapper.insert(record);

        try {
            // 更新版本状态
            version.setStatus(MetaStatus.VERSION_PUBLISHED);
            version.setPublishedAt(LocalDateTime.now());
            version.setPublishedBy(UserContext.getUser().getUsername());
            versionMapper.updateById(version);

            // 更新模型状态
            model.setStatus(MetaStatus.MODEL_PUBLISHED);
            modelMapper.updateById(model);

            // 更新记录状态
            record.setStatus(MetaStatus.RECORD_SUCCESS);
            recordMapper.updateById(record);

            // 记录审计日志
            metaAuditService.logOperation(
                    MetaStatus.AUDIT_PUBLISH, "MODEL", model.getId(),
                    model.getModelCode(), model.getModelName(), null, version, null
            );

            log.info("发布成功: versionId={}, version={}", versionId, version.getVersion());

        } catch (Exception e) {
            // 发布失败
            record.setStatus(MetaStatus.RECORD_FAILED);
            record.setErrorMessage(e.getMessage());
            recordMapper.updateById(record);

            log.error("发布失败: versionId={}, error={}", versionId, e.getMessage());
            throw e;
        }

        // 转换为DTO
        MetaPublishRecordDTO dto = new MetaPublishRecordDTO();
        dto.setId(record.getId());
        dto.setRecordCode(record.getRecordCode());
        dto.setModelId(record.getModelId());
        dto.setVersionTo(record.getVersionTo());
        dto.setActionType(record.getActionType());
        dto.setStatus(record.getStatus());
        dto.setOperator(record.getOperator());
        dto.setOperateTime(record.getOperateTime());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackPublish(Long recordId) {
        MetaPublishRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "发布记录不存在");
        }

        if (!MetaStatus.RECORD_FAILED.equals(record.getStatus())) {
            throw new BusinessException(400, "只有失败的发布可以回滚");
        }

        // 创建新的回滚发布记录
        MetaPublishRecord rollbackRecord = new MetaPublishRecord();
        rollbackRecord.setRecordCode("ROLL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        rollbackRecord.setModelId(record.getModelId());
        rollbackRecord.setVersionFrom(record.getVersionTo());
        rollbackRecord.setVersionTo(record.getVersionFrom() != null ? record.getVersionFrom() : 0);
        rollbackRecord.setActionType(MetaStatus.ACTION_ROLLBACK);
        rollbackRecord.setStatus(MetaStatus.RECORD_SUCCESS);
        rollbackRecord.setOperator(UserContext.getUser().getUsername());
        rollbackRecord.setOperateTime(LocalDateTime.now());

        recordMapper.insert(rollbackRecord);

        log.info("回滚成功: recordId={}", recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveVersion(Long versionId) {
        MetaModelVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(404, "版本不存在");
        }

        if (!MetaStatus.VERSION_PUBLISHED.equals(version.getStatus())) {
            throw new BusinessException(400, "只有已发布版本可以归档");
        }

        version.setStatus(MetaStatus.VERSION_ARCHIVED);
        version.setArchivedAt(LocalDateTime.now());
        version.setArchivedBy(UserContext.getUser().getUsername());
        versionMapper.updateById(version);

        // 记录审计日志
        MetaModel model = modelMapper.selectById(version.getModelId());
        metaAuditService.logOperation(
                MetaStatus.AUDIT_ARCHIVE, "MODEL", model.getId(),
                model.getModelCode(), model.getModelName(), version, null, null
        );

        log.info("归档版本成功: versionId={}", versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MetaPublishRecordDTO rollbackToVersion(Long modelId, Integer targetVersion) {
        MetaModel model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException(404, "模型不存在");
        }

        // 查找目标版本
        LambdaQueryWrapper<MetaModelVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModelVersion::getModelId, modelId)
                .eq(MetaModelVersion::getVersion, targetVersion);
        MetaModelVersion targetVer = versionMapper.selectOne(wrapper);

        if (targetVer == null) {
            throw new BusinessException(404, "目标版本不存在");
        }

        // 创建回滚发布记录
        MetaPublishRecord record = new MetaPublishRecord();
        record.setRecordCode("ROLL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setModelId(modelId);
        record.setVersionFrom(model.getVersion());
        record.setVersionTo(targetVersion);
        record.setActionType(MetaStatus.ACTION_ROLLBACK);
        record.setStatus(MetaStatus.RECORD_SUCCESS);
        record.setOperator(UserContext.getUser().getUsername());
        record.setOperateTime(LocalDateTime.now());

        recordMapper.insert(record);

        // 更新模型当前版本
        model.setVersion(targetVersion);
        model.setStatus(MetaStatus.MODEL_PUBLISHED);
        modelMapper.updateById(model);

        // 记录审计日志
        metaAuditService.logOperation(
                MetaStatus.AUDIT_ROLLBACK, "MODEL", modelId,
                model.getModelCode(), model.getModelName(),
                "v" + record.getVersionFrom(), "v" + targetVersion, null
        );

        log.info("回滚到版本成功: modelId={}, targetVersion={}", modelId, targetVersion);

        // 转换为DTO
        MetaPublishRecordDTO dto = new MetaPublishRecordDTO();
        dto.setId(record.getId());
        dto.setRecordCode(record.getRecordCode());
        dto.setModelId(record.getModelId());
        dto.setVersionFrom(record.getVersionFrom());
        dto.setVersionTo(record.getVersionTo());
        dto.setActionType(record.getActionType());
        dto.setStatus(record.getStatus());
        dto.setOperator(record.getOperator());
        dto.setOperateTime(record.getOperateTime());

        return dto;
    }

    @Override
    public List<MetaModelVersion> getVersionHistory(Long modelId) {
        LambdaQueryWrapper<MetaModelVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModelVersion::getModelId, modelId)
                .orderByDesc(MetaModelVersion::getVersion);
        return versionMapper.selectList(wrapper);
    }

    @Override
    public MetaModelVersion getActiveVersion(Long modelId) {
        MetaModel model = modelMapper.selectById(modelId);
        if (model == null) {
            return null;
        }

        LambdaQueryWrapper<MetaModelVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaModelVersion::getModelId, modelId)
                .eq(MetaModelVersion::getVersion, model.getVersion());
        return versionMapper.selectOne(wrapper);
    }

    @Override
    public Object compareVersions(Long versionId1, Long versionId2) {
        // 版本比较逻辑 - 返回差异信息
        MetaModelVersion v1 = versionMapper.selectById(versionId1);
        MetaModelVersion v2 = versionMapper.selectById(versionId2);

        if (v1 == null || v2 == null) {
            throw new BusinessException(404, "版本不存在");
        }

        // 返回简单的比较信息
        return java.util.Map.of(
                "version1", v1.getVersion(),
                "version2", v2.getVersion(),
                "status1", v1.getStatus(),
                "status2", v2.getStatus()
        );
    }
}
