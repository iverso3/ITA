package com.bank.itarch.meta.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 拓扑图DTO
 */
@Data
public class MetaGraphDTO {

    /** 节点列表 */
    private List<MetaGraphNodeDTO> nodes;

    /** 边列表 */
    private List<MetaGraphEdgeDTO> edges;

    /** 统计信息 */
    private MetaGraphStatsDTO stats;

    @Data
    public static class MetaGraphNodeDTO {
        private Long entityId;
        private String entityCode;
        private String entityName;
        private String modelCode;
        private String modelName;
        private Integer depth;
        private String path;
        private String status;
    }

    @Data
    public static class MetaGraphEdgeDTO {
        private Long relationId;
        private String relCode;
        private String relName;
        private String relType;
        private Long sourceEntityId;
        private Long targetEntityId;
        private String relationLevel;
        private String description;
    }

    @Data
    public static class MetaGraphStatsDTO {
        private Long totalNodes;
        private Long totalEdges;
        private Long isolatedNodes;
        private Boolean hasCycle;
    }
}
