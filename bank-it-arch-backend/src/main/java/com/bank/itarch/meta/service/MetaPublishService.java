package com.bank.itarch.meta.service;

import com.bank.itarch.meta.model.dto.MetaModelVersionDTO;
import com.bank.itarch.meta.model.dto.MetaPublishRecordDTO;
import com.bank.itarch.meta.model.entity.MetaModelVersion;

import java.util.List;

/**
 * 发布管理服务接口
 */
public interface MetaPublishService {

    /**
     * 提交发布（草稿 -> 待发布）
     *
     * @param modelId 模型ID
     * @return 创建的版本
     */
    MetaModelVersion submitPublish(Long modelId);

    /**
     * 审核发布
     *
     * @param versionId 版本ID
     * @param approveComment 审核意见
     */
    void approvePublish(Long versionId, String approveComment);

    /**
     * 执行发布（生成/更新物理表，同步配置）
     *
     * @param versionId 版本ID
     * @return 发布记录
     */
    MetaPublishRecordDTO executePublish(Long versionId);

    /**
     * 发布失败回滚
     *
     * @param recordId 发布记录ID
     */
    void rollbackPublish(Long recordId);

    /**
     * 归档版本
     *
     * @param versionId 版本ID
     */
    void archiveVersion(Long versionId);

    /**
     * 回滚到指定版本
     *
     * @param modelId 模型ID
     * @param targetVersion 目标版本
     * @return 发布记录
     */
    MetaPublishRecordDTO rollbackToVersion(Long modelId, Integer targetVersion);

    /**
     * 获取模型版本历史
     *
     * @param modelId 模型ID
     * @return 版本列表
     */
    List<MetaModelVersion> getVersionHistory(Long modelId);

    /**
     * 获取当前生效版本
     *
     * @param modelId 模型ID
     * @return 当前版本
     */
    MetaModelVersion getActiveVersion(Long modelId);

    /**
     * 比较两个版本的差异
     *
     * @param versionId1 版本ID1
     * @param versionId2 版本ID2
     * @return 差异DTO
     */
    Object compareVersions(Long versionId1, Long versionId2);
}
