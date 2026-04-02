package com.bank.itarch.meta.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bank.itarch.common.PageQuery;
import com.bank.itarch.common.PageResult;
import com.bank.itarch.meta.model.dto.MetaModelDTO;
import com.bank.itarch.meta.model.entity.MetaModel;

import java.util.List;

/**
 * 元模型管理服务接口
 * 提供元模型的CRUD、分页查询、详情获取等能力
 */
public interface MetaModelService {

    /**
     * 创建元模型（草稿状态）
     *
     * @param dto 元模型信息
     * @return 创建的元模型
     */
    MetaModel createModel(MetaModelDTO dto);

    /**
     * 更新元模型（仅草稿状态可修改）
     *
     * @param id 元模型ID
     * @param dto 元模型信息
     * @return 更新后的元模型
     */
    MetaModel updateModel(Long id, MetaModelDTO dto);

    /**
     * 删除元模型（仅草稿状态可删除）
     *
     * @param id 元模型ID
     */
    void deleteModel(Long id);

    /**
     * 分页查询元模型
     *
     * @param query 分页参数
     * @param keyword 关键词（搜索modelCode/modelName）
     * @param modelType 模型类型
     * @param status 状态
     * @return 分页结果
     */
    PageResult<MetaModel> pageQuery(PageQuery query, String keyword, String modelType, String status);

    /**
     * 获取元模型详情（含字段、分组、关系）
     *
     * @param id 元模型ID
     * @return 元模型详情DTO
     */
    MetaModelDTO getModelDetail(Long id);

    /**
     * 通过编码获取元模型
     *
     * @param modelCode 模型编码
     * @return 元模型
     */
    MetaModel getByCode(String modelCode);

    /**
     * 获取所有启用的元模型
     *
     * @return 元模型列表
     */
    List<MetaModel> listAllActive();

    /**
     * 根据模型类型查询
     *
     * @param modelType 模型类型
     * @return 元模型列表
     */
    List<MetaModel> listByType(String modelType);
}
