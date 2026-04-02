package com.bank.itarch.engine.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bank.itarch.engine.WfEngineContext;
import com.bank.itarch.engine.execution.WfToken;
import com.bank.itarch.model.entity.WfDefinitionLine;
import com.bank.itarch.model.entity.WfDefinitionNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 条件分支节点处理器
 */
@Slf4j
@Component
public class ConditionNodeHandler extends AbstractNodeHandler {

    @Override
    public String getNodeType() {
        return "CONDITION";
    }

    @Override
    protected WfDefinitionLine selectNextLine(WfEngineContext context, WfToken token,
                                                List<WfDefinitionLine> outgoingLines) {
        WfDefinitionNode currentNode = context.getNodeById(token.getCurrentNodeId());
        if (currentNode == null || outgoingLines.isEmpty()) {
            return outgoingLines.isEmpty() ? null : outgoingLines.get(0);
        }

        // 解析条件表达式
        String conditionRule = currentNode.getConditionRule();
        if (conditionRule == null || conditionRule.isEmpty()) {
            return outgoingLines.get(0);
        }

        try {
            JSONObject conditionConfig = JSON.parseObject(conditionRule);
            JSONArray conditions = conditionConfig.getJSONArray("conditions");

            if (conditions == null || conditions.isEmpty()) {
                return outgoingLines.get(0);
            }

            // 遍历条件，找到第一个满足的条件
            for (int i = 0; i < conditions.size(); i++) {
                JSONObject condition = conditions.getJSONObject(i);
                String expression = condition.getString("expression");
                Long targetNodeId = condition.getLong("targetNodeId");

                if (expression != null && evaluateExpression(context, expression)) {
                    // 找到满足条件的连线
                    return outgoingLines.stream()
                            .filter(l -> l.getTargetNodeId().equals(targetNodeId))
                            .findFirst()
                            .orElse(outgoingLines.get(0));
                }
            }

            // 没有满足的条件，返回默认连线
            Long defaultNodeId = conditionConfig.getLong("defaultNodeId");
            if (defaultNodeId != null) {
                return outgoingLines.stream()
                        .filter(l -> l.getTargetNodeId().equals(defaultNodeId))
                        .findFirst()
                        .orElse(outgoingLines.get(0));
            }

        } catch (Exception e) {
            log.error("解析条件表达式失败: {}", conditionRule, e);
        }

        return outgoingLines.get(0);
    }

    /**
     * 简单表达式求值
     * 支持: amount > 10000, status == 'APPROVED', etc.
     */
    private boolean evaluateExpression(WfEngineContext context, String expression) {
        try {
            // 简单实现：支持基本的比较运算
            // 实际生产环境建议使用规则引擎如Drools
            String expr = expression.trim();

            // 替换变量
            for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                String varName = entry.getKey();
                Object varValue = entry.getValue();
                String replacement = varValue != null ? varValue.toString() : "null";
                expr = expr.replace(varName, replacement);
            }

            // 解析表达式
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                return Double.parseDouble(parts[0].trim()) >= Double.parseDouble(parts[1].trim());
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                return Double.parseDouble(parts[0].trim()) <= Double.parseDouble(parts[1].trim());
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                return !parts[0].trim().equals(parts[1].trim());
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                return parts[0].trim().equals(parts[1].trim());
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                return Double.parseDouble(parts[0].trim()) > Double.parseDouble(parts[1].trim());
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                return Double.parseDouble(parts[0].trim()) < Double.parseDouble(parts[1].trim());
            }

            return Boolean.parseBoolean(expr);
        } catch (Exception e) {
            log.error("表达式求值失败: {}", expression, e);
            return false;
        }
    }
}
