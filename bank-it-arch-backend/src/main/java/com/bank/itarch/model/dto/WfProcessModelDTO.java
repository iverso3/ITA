package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 流程模型DTO（用于建模器）
 */
@Data
public class WfProcessModelDTO implements Serializable {
    private Long definitionId;
    private String definitionCode;
    private String definitionName;
    private String processType;
    private Integer version;
    private String status;
    private List<WfNodeDTO> nodes;
    private List<WfLineDTO> lines;
}
