package com.bank.itarch.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer total;        // 总行数（不含表头）
    private Integer success;      // 成功行数
    private Integer failed;       // 失败行数
    private List<ImportError> errors = new ArrayList<>();

    @Data
    public static class ImportError implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer row;      // 行号（从1开始，表头为0）
        private String reason;     // 失败原因

        public ImportError(Integer row, String reason) {
            this.row = row;
            this.reason = reason;
        }
    }

    public static ImportResult ok(int total, int success) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFailed(total - success);
        return result;
    }

    public static ImportResult error(int total, List<ImportError> errors) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setFailed(errors.size());
        result.setSuccess(total - errors.size());
        result.setErrors(errors);
        return result;
    }
}