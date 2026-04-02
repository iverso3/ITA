package com.bank.itarch.meta.service;

import com.bank.itarch.meta.model.dto.MetaGroupDTO;
import com.bank.itarch.meta.model.entity.MetaGroup;

import java.util.List;

/**
 * 字段分组管理服务接口
 */
public interface MetaGroupService {

    /**
     * 创建分组
     *
     * @param modelId 模型ID
     * @param dto 分组信息
     * @return 创建的分组
     */
    MetaGroup createGroup(Long modelId, MetaGroupDTO dto);

    /**
     * 更新分组
     *
     * @param modelId 模型ID
     * @param groupId 分组ID
     * @param dto 分组信息
     * @return 更新后的分组
     */
    MetaGroup updateGroup(Long modelId, Long groupId, MetaGroupDTO dto);

    /**
     * 删除分组
     *
     * @param modelId 模型ID
     * @param groupId 分组ID
     */
    void deleteGroup(Long modelId, Long groupId);

    /**
     * 获取模型的所有分组
     *
     * @param modelId 模型ID
     * @return 分组列表
     */
    List<MetaGroup> getGroupsByModel(Long modelId);

    /**
     * 获取模型的分组（含字段）
     *
     * @param modelId 模型ID
     * @return 分组列表（含字段）
     */
    List<MetaGroupDTO> getGroupsWithFields(Long modelId);
}
