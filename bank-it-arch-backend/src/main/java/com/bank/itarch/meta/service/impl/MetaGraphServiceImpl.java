package com.bank.itarch.meta.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bank.itarch.common.BusinessException;
import com.bank.itarch.meta.mapper.MetaEntityMapper;
import com.bank.itarch.meta.mapper.MetaRelationMapper;
import com.bank.itarch.meta.model.dto.MetaGraphDTO;
import com.bank.itarch.meta.model.dto.MetaRelationDTO;
import com.bank.itarch.meta.model.entity.MetaEntity;
import com.bank.itarch.meta.model.entity.MetaRelation;
import com.bank.itarch.meta.service.MetaGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 拓扑查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaGraphServiceImpl implements MetaGraphService {

    private final MetaEntityMapper metaEntityMapper;
    private final MetaRelationMapper metaRelationMapper;

    @Override
    public MetaGraphDTO getEntityGraph(Long entityId, Integer depth) {
        if (depth == null || depth < 1) {
            depth = 1;
        }

        MetaEntity entity = metaEntityMapper.selectById(entityId);
        if (entity == null) {
            throw new BusinessException(404, "实体不存在");
        }

        MetaGraphDTO graph = new MetaGraphDTO();

        // 获取直接关联的实体
        Set<Long> visitedEntityIds = new HashSet<>();
        visitedEntityIds.add(entityId);

        List<MetaGraphDTO.MetaGraphNodeDTO> nodes = new ArrayList<>();
        List<MetaGraphDTO.MetaGraphEdgeDTO> edges = new ArrayList<>();

        // 添加起始节点
        MetaGraphDTO.MetaGraphNodeDTO startNode = new MetaGraphDTO.MetaGraphNodeDTO();
        startNode.setEntityId(entity.getId());
        startNode.setEntityCode(entity.getEntityCode());
        startNode.setEntityName(entity.getEntityName());
        startNode.setModelCode(entity.getModelCode());
        startNode.setDepth(0);
        nodes.add(startNode);

        // 递归获取关联实体
        buildGraphRecursive(entityId, depth, visitedEntityIds, nodes, edges);

        // 统计信息
        MetaGraphDTO.MetaGraphStatsDTO stats = new MetaGraphDTO.MetaGraphStatsDTO();
        stats.setTotalNodes((long) nodes.size());
        stats.setTotalEdges((long) edges.size());
        stats.setHasCycle(detectCycle(entity.getModelId()));

        graph.setNodes(nodes);
        graph.setEdges(edges);
        graph.setStats(stats);

        return graph;
    }

    private void buildGraphRecursive(Long entityId, int remainingDepth,
                                     Set<Long> visitedEntityIds,
                                     List<MetaGraphDTO.MetaGraphNodeDTO> nodes,
                                     List<MetaGraphDTO.MetaGraphEdgeDTO> edges) {
        if (remainingDepth <= 0) {
            return;
        }

        // 获取源实体发出的关系
        LambdaQueryWrapper<MetaRelation> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(MetaRelation::getSourceEntityId, entityId)
                .eq(MetaRelation::getIsActive, true);
        List<MetaRelation> outRelations = metaRelationMapper.selectList(outWrapper);

        // 获取指向源实体的关系
        LambdaQueryWrapper<MetaRelation> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(MetaRelation::getTargetEntityId, entityId)
                .eq(MetaRelation::getIsActive, true);
        List<MetaRelation> inRelations = metaRelationMapper.selectList(inWrapper);

        // 处理发出的关系
        for (MetaRelation rel : outRelations) {
            if (!visitedEntityIds.contains(rel.getTargetEntityId())) {
                visitedEntityIds.add(rel.getTargetEntityId());

                // 添加目标节点
                MetaEntity target = metaEntityMapper.selectById(rel.getTargetEntityId());
                if (target != null) {
                    MetaGraphDTO.MetaGraphNodeDTO node = new MetaGraphDTO.MetaGraphNodeDTO();
                    node.setEntityId(target.getId());
                    node.setEntityCode(target.getEntityCode());
                    node.setEntityName(target.getEntityName());
                    node.setModelCode(target.getModelCode());
                    node.setDepth(calculateDepth(nodes, entityId) + 1);
                    nodes.add(node);
                }

                // 添加边
                addEdge(rel, edges);
            }

            // 递归处理
            buildGraphRecursive(rel.getTargetEntityId(), remainingDepth - 1,
                    visitedEntityIds, nodes, edges);
        }

        // 处理指向的关系
        for (MetaRelation rel : inRelations) {
            if (!visitedEntityIds.contains(rel.getSourceEntityId())) {
                visitedEntityIds.add(rel.getSourceEntityId());

                // 添加源节点
                MetaEntity source = metaEntityMapper.selectById(rel.getSourceEntityId());
                if (source != null) {
                    MetaGraphDTO.MetaGraphNodeDTO node = new MetaGraphDTO.MetaGraphNodeDTO();
                    node.setEntityId(source.getId());
                    node.setEntityCode(source.getEntityCode());
                    node.setEntityName(source.getEntityName());
                    node.setModelCode(source.getModelCode());
                    node.setDepth(calculateDepth(nodes, entityId) + 1);
                    nodes.add(node);
                }

                // 添加边
                addEdge(rel, edges);
            }
        }
    }

    private int calculateDepth(List<MetaGraphDTO.MetaGraphNodeDTO> nodes, Long entityId) {
        return nodes.stream()
                .filter(n -> n.getEntityId().equals(entityId))
                .findFirst()
                .map(MetaGraphDTO.MetaGraphNodeDTO::getDepth)
                .orElse(0);
    }

    private void addEdge(MetaRelation rel, List<MetaGraphDTO.MetaGraphEdgeDTO> edges) {
        MetaGraphDTO.MetaGraphEdgeDTO edge = new MetaGraphDTO.MetaGraphEdgeDTO();
        edge.setRelationId(rel.getId());
        edge.setRelCode(rel.getRelCode());
        edge.setRelType(rel.getRelType());
        edge.setSourceEntityId(rel.getSourceEntityId());
        edge.setTargetEntityId(rel.getTargetEntityId());
        edge.setRelationLevel(rel.getRelationLevel());
        edge.setDescription(rel.getDescription());
        edges.add(edge);
    }

    @Override
    public MetaGraphDTO getModelGraph(Long modelId, Object filter) {
        // 获取模型下所有实体
        LambdaQueryWrapper<MetaEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(MetaEntity::getModelId, modelId)
                .eq(MetaEntity::getStatus, "ACTIVE");
        List<MetaEntity> entities = metaEntityMapper.selectList(entityWrapper);

        MetaGraphDTO graph = new MetaGraphDTO();
        List<MetaGraphDTO.MetaGraphNodeDTO> nodes = new ArrayList<>();
        List<MetaGraphDTO.MetaGraphEdgeDTO> edges = new ArrayList<>();

        Set<Long> entityIds = new HashSet<>();
        for (MetaEntity entity : entities) {
            entityIds.add(entity.getId());

            MetaGraphDTO.MetaGraphNodeDTO node = new MetaGraphDTO.MetaGraphNodeDTO();
            node.setEntityId(entity.getId());
            node.setEntityCode(entity.getEntityCode());
            node.setEntityName(entity.getEntityName());
            node.setModelCode(entity.getModelCode());
            node.setStatus(entity.getStatus());
            nodes.add(node);
        }

        // 获取所有关系
        LambdaQueryWrapper<MetaRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(relWrapper);

        for (MetaRelation rel : relations) {
            if (entityIds.contains(rel.getSourceEntityId()) &&
                    entityIds.contains(rel.getTargetEntityId())) {
                addEdge(rel, edges);
            }
        }

        // 统计信息
        MetaGraphDTO.MetaGraphStatsDTO stats = new MetaGraphDTO.MetaGraphStatsDTO();
        stats.setTotalNodes((long) nodes.size());
        stats.setTotalEdges((long) edges.size());
        stats.setIsolatedNodes(countIsolatedNodes(nodes, edges));
        stats.setHasCycle(detectCycle(modelId));

        graph.setNodes(nodes);
        graph.setEdges(edges);
        graph.setStats(stats);

        return graph;
    }

    @Override
    public List<Object> recursiveQuery(Long entityId, String relCode, Integer maxDepth) {
        if (maxDepth == null || maxDepth < 1) {
            maxDepth = 3;
        }

        List<Object> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        recursiveQueryInternal(entityId, relCode, maxDepth, visited, result, 0);
        return result;
    }

    private void recursiveQueryInternal(Long entityId, String relCode, int maxDepth,
                                        Set<Long> visited, List<Object> result, int currentDepth) {
        if (currentDepth >= maxDepth || visited.contains(entityId)) {
            return;
        }

        visited.add(entityId);

        MetaEntity entity = metaEntityMapper.selectById(entityId);
        if (entity != null) {
            result.add(java.util.Map.of(
                    "entity", entity,
                    "depth", currentDepth
            ));
        }

        // 获取关联实体
        LambdaQueryWrapper<MetaRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetaRelation::getSourceEntityId, entityId)
                .eq(MetaRelation::getRelCode, relCode)
                .eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(wrapper);

        for (MetaRelation rel : relations) {
            recursiveQueryInternal(rel.getTargetEntityId(), relCode,
                    maxDepth, visited, result, currentDepth + 1);
        }
    }

    @Override
    public List<Object> shortestPath(Long sourceEntityId, Long targetEntityId) {
        // 简单的BFS查找最短路径
        if (sourceEntityId.equals(targetEntityId)) {
            return List.of(java.util.Map.of("entityId", sourceEntityId, "depth", 0));
        }

        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Long> predecessor = new HashMap<>();
        Queue<Long> queue = new LinkedList<>();

        distance.put(sourceEntityId, 0);
        queue.offer(sourceEntityId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            if (current.equals(targetEntityId)) {
                break;
            }

            // 获取所有关联实体
            LambdaQueryWrapper<MetaRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MetaRelation::getSourceEntityId, current)
                    .eq(MetaRelation::getIsActive, true);
            List<MetaRelation> relations = metaRelationMapper.selectList(wrapper);

            for (MetaRelation rel : relations) {
                Long neighbor = rel.getTargetEntityId();
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, distance.get(current) + 1);
                    predecessor.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        // 重建路径
        if (!distance.containsKey(targetEntityId)) {
            return List.of(); // 无路径
        }

        List<Object> path = new ArrayList<>();
        Long current = targetEntityId;
        while (current != null) {
            MetaEntity entity = metaEntityMapper.selectById(current);
            if (entity != null) {
                path.add(0, java.util.Map.of(
                        "entityId", entity.getId(),
                        "entityCode", entity.getEntityCode(),
                        "entityName", entity.getEntityName()
                ));
            }
            current = predecessor.get(current);
        }

        return path;
    }

    @Override
    public MetaGraphDTO batchQueryGraph(List<Long> entityIds, Integer depth) {
        if (entityIds == null || entityIds.isEmpty()) {
            return new MetaGraphDTO();
        }

        MetaGraphDTO graph = new MetaGraphDTO();
        List<MetaGraphDTO.MetaGraphNodeDTO> nodes = new ArrayList<>();
        List<MetaGraphDTO.MetaGraphEdgeDTO> edges = new ArrayList<>();

        for (Long entityId : entityIds) {
            MetaGraphDTO entityGraph = getEntityGraph(entityId, depth);
            nodes.addAll(entityGraph.getNodes());
            edges.addAll(entityGraph.getEdges());
        }

        // 去重
        Map<Long, MetaGraphDTO.MetaGraphNodeDTO> nodeMap = new LinkedHashMap<>();
        for (MetaGraphDTO.MetaGraphNodeDTO node : nodes) {
            nodeMap.putIfAbsent(node.getEntityId(), node);
        }
        nodes = new ArrayList<>(nodeMap.values());

        graph.setNodes(nodes);
        graph.setEdges(edges);

        MetaGraphDTO.MetaGraphStatsDTO stats = new MetaGraphDTO.MetaGraphStatsDTO();
        stats.setTotalNodes((long) nodes.size());
        stats.setTotalEdges((long) edges.size());
        graph.setStats(stats);

        return graph;
    }

    @Override
    public boolean detectCycle(Long modelId) {
        // 检测模型的关系图中是否存在环
        LambdaQueryWrapper<MetaEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(MetaEntity::getModelId, modelId);
        List<MetaEntity> entities = metaEntityMapper.selectList(entityWrapper);

        Map<Long, List<Long>> adjacency = new HashMap<>();
        for (MetaEntity entity : entities) {
            adjacency.put(entity.getId(), new ArrayList<>());
        }

        LambdaQueryWrapper<MetaRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(relWrapper);

        for (MetaRelation rel : relations) {
            if (adjacency.containsKey(rel.getSourceEntityId())) {
                adjacency.get(rel.getSourceEntityId()).add(rel.getTargetEntityId());
            }
        }

        // DFS检测环
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Long entityId : adjacency.keySet()) {
            if (hasCycleDFS(entityId, adjacency, visited, recursionStack)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasCycleDFS(Long node, Map<Long, List<Long>> adjacency,
                                Set<Long> visited, Set<Long> recursionStack) {
        if (recursionStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (Long neighbor : adjacency.getOrDefault(node, List.of())) {
            if (hasCycleDFS(neighbor, adjacency, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    @Override
    public List<Object> getIsolatedEntities(Long modelId) {
        MetaGraphDTO graph = getModelGraph(modelId, null);
        Set<Long> connectedEntities = new HashSet<>();

        for (MetaGraphDTO.MetaGraphEdgeDTO edge : graph.getEdges()) {
            connectedEntities.add(edge.getSourceEntityId());
            connectedEntities.add(edge.getTargetEntityId());
        }

        return graph.getNodes().stream()
                .filter(node -> !connectedEntities.contains(node.getEntityId()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> countRelationsByType(Long modelId) {
        LambdaQueryWrapper<MetaEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(MetaEntity::getModelId, modelId);
        List<MetaEntity> entities = metaEntityMapper.selectList(entityWrapper);

        Set<Long> entityIds = entities.stream()
                .map(MetaEntity::getId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<MetaRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(MetaRelation::getIsActive, true);
        List<MetaRelation> relations = metaRelationMapper.selectList(relWrapper);

        Map<String, Long> counts = new HashMap<>();
        for (MetaRelation rel : relations) {
            if (entityIds.contains(rel.getSourceEntityId()) ||
                    entityIds.contains(rel.getTargetEntityId())) {
                counts.merge(rel.getRelType(), 1L, Long::sum);
            }
        }

        return counts;
    }

    private long countIsolatedNodes(List<MetaGraphDTO.MetaGraphNodeDTO> nodes,
                                    List<MetaGraphDTO.MetaGraphEdgeDTO> edges) {
        Set<Long> connected = new HashSet<>();
        for (MetaGraphDTO.MetaGraphEdgeDTO edge : edges) {
            connected.add(edge.getSourceEntityId());
            connected.add(edge.getTargetEntityId());
        }

        return nodes.stream()
                .filter(node -> !connected.contains(node.getEntityId()))
                .count();
    }
}
