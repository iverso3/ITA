package com.bank.itarch.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;

/**
 * 流程连线DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WfLineDTO implements Serializable {
    private Long id;
    private Long definitionId;
    // 使用Object以便同时接受前端字符串ID和数据库Long ID
    private Object sourceNodeId;
    private Object targetNodeId;
    private String lineCode;
    private String lineName;
    private String conditionExpr;
    private Integer lineOrder;

    // Vue Flow连线ID
    private String source;
    private String target;
    private String sourceHandle;
    private String targetHandle;
}
