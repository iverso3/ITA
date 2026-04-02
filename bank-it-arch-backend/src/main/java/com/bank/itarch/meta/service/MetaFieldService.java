package com.bank.itarch.meta.service;

import com.bank.itarch.meta.model.dto.MetaFieldDTO;
import com.bank.itarch.meta.model.entity.MetaField;

import java.util.List;

/**
 * 字段定义管理服务接口
 */
public interface MetaFieldService {

    /**
     * 添加字段
     *
     * @param modelId 模型ID
     * @param dto 字段信息
     * @return 创建的字段
     */
    MetaField addField(Long modelId, MetaFieldDTO dto);

    /**
     * 更新字段
     *
     * @param modelId 模型ID
     * @param fieldId 字段ID
     * @param dto 字段信息
     * @return 更新后的字段
     */
    MetaField updateField(Long modelId, Long fieldId, MetaFieldDTO dto);

    /**
     * 删除字段
     *
     * @param modelId 模型ID
     * @param fieldId 字段ID
     */
    void deleteField(Long modelId, Long fieldId);

    /**
     * 批量添加字段
     *
     * @param modelId 模型ID
     * @param fields 字段列表
     * @return 创建的字段列表
     */
    List<MetaField> batchAddFields(Long modelId, List<MetaFieldDTO> fields);

    /**
     * 调整字段顺序
     *
     * @param modelId 模型ID
     * @param fieldIds 字段ID列表（按排序顺序）
     */
    void reorderFields(Long modelId, List<Long> fieldIds);

    /**
     * 获取模型的所有字段
     *
     * @param modelId 模型ID
     * @return 字段列表
     */
    List<MetaField> getFieldsByModel(Long modelId);

    /**
     * 获取模型的可查询字段
     *
     * @param modelId 模型ID
     * @return 可查询字段列表
     */
    List<MetaField> getQueryableFields(Long modelId);

    /**
     * 获取模型的敏感字段
     *
     * @param modelId 模型ID
     * @return 敏感字段列表
     */
    List<MetaField> getSensitiveFields(Long modelId);

    /**
     * 通过编码获取字段
     *
     * @param modelId 模型ID
     * @param fieldCode 字段编码
     * @return 字段
     */
    MetaField getByCode(Long modelId, String fieldCode);
}
