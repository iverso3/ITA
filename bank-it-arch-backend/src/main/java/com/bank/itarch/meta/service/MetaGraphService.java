package com.bank.itarch.meta.service;

import com.bank.itarch.meta.model.dto.MetaGraphDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拓扑查询服务接口
 */
public interface MetaGraphService {

    /**
     * 获取实体拓扑图（指定实体的直接关联）
     *
     * @param entityId 实体ID
     * @param depth 深度
     * @return 拓扑图
     */
    MetaGraphDTO getEntityGraph(Long entityId, Integer depth);

    /**
     * 获取模型拓扑图（指定模型的所有实体及其关系）
     *
     * @param modelId 模型ID
     * @param filter 过滤条件
     * @return 拓扑图
     */
    MetaGraphDTO getModelGraph(Long modelId, Object filter);

    /**
     * 递归查询实体关系（支持N层深度）
     *
     * @param entityId 实体ID
     * @param relCode 关系编码
     * @param maxDepth 最大深度
     * @return 关系节点列表
     */
    List<Object> recursiveQuery(Long entityId, String relCode, Integer maxDepth);

    /**
     * 查询最短路径（两个实体之间）
     *
     * @param sourceEntityId 源实体ID
     * @param targetEntityId 目标实体ID
     * @return 最短路径
     */
    List<Object> shortestPath(Long sourceEntityId, Long targetEntityId);

    /**
     * 批量查询实体关系（用于图可视化）
     *
     * @param entityIds 实体ID列表
     * @param depth 深度
     * @return 拓扑图
     */
    MetaGraphDTO batchQueryGraph(List<Long> entityIds, Integer depth);

    /**
     * 环检测（检测关系是否成环）
     *
     * @param modelId 模型ID
     * @return 是否存在环
     */
    boolean detectCycle(Long modelId);

    /**
     * 获取孤立的实体（没有关联）
     *
     * @param modelId 模型ID
     * @return 孤立实体列表
     */
    List<Object> getIsolatedEntities(Long modelId);

    /**
     * 统计关系数量（按类型）
     *
     * @param modelId 模型ID
     * @return 关系数量Map
     */
    Map<String, Long> countRelationsByType(Long modelId);
}
