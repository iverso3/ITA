package com.bank.itarch.meta.service;

import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.model.dto.MetaEntityDTO;
import com.bank.itarch.meta.model.dto.MetaRelationDTO;
import com.bank.itarch.meta.model.entity.MetaEntity;

import java.util.List;
import java.util.Map;

/**
 * 实体管理服务接口
 */
public interface MetaEntityService {

    /**
     * 创建实体
     *
     * @param dto 实体信息
     * @return 创建的实体
     */
    MetaEntity createEntity(MetaEntityDTO dto);

    /**
     * 批量创建实体
     *
     * @param modelId 模型ID
     * @param entities 实体列表
     * @return 创建的实体列表
     */
    List<MetaEntity> batchCreate(Long modelId, List<MetaEntityDTO> entities);

    /**
     * 更新实体
     *
     * @param id 实体ID
     * @param dto 实体信息
     * @return 更新后的实体
     */
    MetaEntity updateEntity(Long id, MetaEntityDTO dto);

    /**
     * 删除实体（软删除）
     *
     * @param id 实体ID
     */
    void deleteEntity(Long id);

    /**
     * 批量删除实体
     *
     * @param modelId 模型ID
     * @param ids 实体ID列表
     */
    void batchDelete(Long modelId, List<Long> ids);

    /**
     * 分页查询实体
     *
     * @param query 分页参数
     * @param modelId 模型ID
     * @param keyword 关键词
     * @param filters 过滤条件
     * @return 分页结果
     */
    PageResult<MetaEntity> pageQuery(PageQuery query, Long modelId, String keyword, Map<String, Object> filters);

    /**
     * 获取实体详情（含所有字段值和关系）
     *
     * @param id 实体ID
     * @return 实体详情DTO
     */
    MetaEntityDTO getEntityDetail(Long id);

    /**
     * 通过业务ID获取实体
     *
     * @param modelId 模型ID
     * @param businessId 业务表主键
     * @return 实体
     */
    MetaEntity getByBusinessId(Long modelId, Long businessId);

    /**
     * 绑定关系
     *
     * @param sourceEntityId 源实体ID
     * @param relCode 关系编码
     * @param targetEntityId 目标实体ID
     */
    void bindRelation(Long sourceEntityId, String relCode, Long targetEntityId);

    /**
     * 解绑关系
     *
     * @param sourceEntityId 源实体ID
     * @param relCode 关系编码
     * @param targetEntityId 目标实体ID
     */
    void unbindRelation(Long sourceEntityId, String relCode, Long targetEntityId);

    /**
     * 获取实体所有关系
     *
     * @param entityId 实体ID
     * @return 关系列表
     */
    List<MetaRelationDTO> getEntityRelations(Long entityId);

    /**
     * 获取实体的关联实体列表（按关系类型）
     *
     * @param entityId 实体ID
     * @param relCode 关系编码
     * @return 关联实体列表
     */
    List<MetaEntity> getRelatedEntities(Long entityId, String relCode);

    /**
     * 设置字段值
     *
     * @param entityId 实体ID
     * @param fieldCode 字段编码
     * @param value 字段值
     */
    void setFieldValue(Long entityId, String fieldCode, Object value);

    /**
     * 批量设置字段值
     *
     * @param entityId 实体ID
     * @param values 字段值Map
     */
    void setFieldValues(Long entityId, Map<String, Object> values);

    /**
     * 获取字段值
     *
     * @param entityId 实体ID
     * @param fieldCode 字段编码
     * @return 字段值
     */
    Object getFieldValue(Long entityId, String fieldCode);

    /**
     * 获取所有字段值
     *
     * @param entityId 实体ID
     * @return 字段值Map
     */
    Map<String, Object> getAllFieldValues(Long entityId);
}
