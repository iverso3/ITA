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
    public void enter(WfEngineContext context, WfToken token) {
        log.info("进入条件节点: nodeId={}, instanceId={}, nodeCode={}",
                token.getCurrentNodeId(), token.getInstance().getId(), token.getCurrentNodeCode());
        super.enter(context, token);

        // 条件节点进入后自动流转到下一节点
        log.info("条件节点完成，自动流转到下一节点");
        flowToNextNode(context, token);
    }

    /**
     * 流转到下一个节点
     */
    private void flowToNextNode(WfEngineContext context, WfToken token) {
        WfDefinitionNode currentNode = context.getNodeById(token.getCurrentNodeId());
        if (currentNode == null) {
            log.error("flowToNextNode: 未找到当前节点: {}", token.getCurrentNodeId());
            return;
        }
        log.info("flowToNextNode(), currentNode={}, currentNodeId={}", currentNode.getNodeCode(), currentNode.getId());

        List<WfDefinitionLine> outgoingLines = context.getOutgoingLines(currentNode.getId());
        log.info("flowToNextNode: 出口连线数量={}", outgoingLines.size());
        if (outgoingLines.isEmpty()) {
            log.warn("flowToNextNode: 节点 {} 没有出口连线", currentNode.getNodeCode());
            return;
        }

        WfDefinitionLine nextLine = selectNextLine(context, token, outgoingLines);
        log.info("flowToNextNode: 选择的连线: {}", nextLine != null ? nextLine.getLineCode() : "null");
        if (nextLine != null) {
            WfDefinitionNode nextNode = context.getNodeById(nextLine.getTargetNodeId());
            log.info("flowToNextNode: 下一节点: {}", nextNode != null ? nextNode.getNodeName() : "null");
            if (nextNode != null) {
                log.info("流转到下一节点: {} -> {}", currentNode.getNodeCode(), nextNode.getNodeCode());

                // 更新令牌
                token.setCurrentNodeId(nextNode.getId());
                token.setCurrentNodeCode(nextNode.getNodeCode());
                token.addHistory(nextNode.getId());

                // 更新数据库中的令牌
                var tokenEntity = context.getTokenMapper().selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.bank.itarch.model.entity.WfToken>()
                        .eq(com.bank.itarch.model.entity.WfToken::getInstanceId, token.getInstance().getId())
                        .eq(com.bank.itarch.model.entity.WfToken::getDeleted, 0));
                if (tokenEntity != null) {
                    tokenEntity.setCurrentNodeId(nextNode.getId());
                    tokenEntity.setTokenStatus(WfToken.STATUS_ACTIVE);
                    context.getTokenMapper().updateById(tokenEntity);
                }

                // 调用下一个节点的enter
                WfNodeHandler handler = context.getNodeHandlers().get(nextNode.getNodeCategory());
                log.info("获取到下一节点处理器: {} -> category={}, handler={}", nextNode.getNodeCode(), nextNode.getNodeCategory(), handler);
                if (handler != null) {
                    handler.enter(context, token);
                } else {
                    log.error("未找到处理器: {}", nextNode.getNodeCategory());
                }
            }
        }
    }

    @Override
    protected WfDefinitionLine selectNextLine(WfEngineContext context, WfToken token,
                                                List<WfDefinitionLine> outgoingLines) {
        WfDefinitionNode currentNode = context.getNodeById(token.getCurrentNodeId());
        if (currentNode == null || outgoingLines.isEmpty()) {
            log.warn("selectNextLine: currentNode={}, outgoingLines.size={}", currentNode, outgoingLines.size());
            return outgoingLines.isEmpty() ? null : outgoingLines.get(0);
        }

        // 解析条件表达式
        String conditionRule = currentNode.getConditionRule();
        log.info("条件节点 conditionRule: {}, nodeName={}, nodeCode={}", conditionRule, currentNode.getNodeName(), currentNode.getNodeCode());
        log.info("outgoingLines 数量: {}", outgoingLines.size());
        for (int i = 0; i < outgoingLines.size(); i++) {
            WfDefinitionLine line = outgoingLines.get(i);
            WfDefinitionNode targetNode = context.getNodeById(line.getTargetNodeId());
            log.info("  连线 {}: lineId={}, targetNodeId={}, targetNodeName={}", i, line.getId(), line.getTargetNodeId(), targetNode != null ? targetNode.getNodeName() : "null");
        }

        if (conditionRule == null || conditionRule.isEmpty()) {
            log.info("conditionRule 为空，返回第一条连线");
            WfDefinitionLine firstLine = outgoingLines.get(0);
            log.info("选择的连线: lineId={}, targetNodeId={}", firstLine.getId(), firstLine.getTargetNodeId());
            return firstLine;
        }

        try {
            JSONObject conditionConfig = JSON.parseObject(conditionRule);
            JSONArray conditions = conditionConfig.getJSONArray("conditions");
            log.info("条件数量: {}", conditions != null ? conditions.size() : 0);

            if (conditions == null || conditions.isEmpty()) {
                log.info("conditions 为空，返回第一条连线");
                WfDefinitionLine firstLine = outgoingLines.get(0);
                log.info("选择的连线: lineId={}, targetNodeId={}", firstLine.getId(), firstLine.getTargetNodeId());
                return firstLine;
            }

            // 遍历条件，找到第一个满足的条件
            for (int i = 0; i < conditions.size(); i++) {
                JSONObject condition = conditions.getJSONObject(i);
                String expression = condition.getString("expression");
                Long targetNodeId = condition.getLong("targetNodeId");
                log.info("条件 {}: expression={}, targetNodeId={}", i, expression, targetNodeId);

                if (expression != null && evaluateExpression(context, expression)) {
                    log.info("条件 {} 满足!", i);
                    // 找到满足条件的连线
                    WfDefinitionLine selectedLine = outgoingLines.stream()
                            .filter(l -> l.getTargetNodeId().equals(targetNodeId))
                            .findFirst()
                            .orElse(outgoingLines.get(0));
                    log.info("选择的连线: lineId={}, targetNodeId={}", selectedLine.getId(), selectedLine.getTargetNodeId());
                    return selectedLine;
                } else {
                    log.info("条件 {} 不满足", i);
                }
            }

            // 没有满足的条件，返回默认连线
            Long defaultNodeId = conditionConfig.getLong("defaultNodeId");
            log.info("没有满足的条件，使用 defaultNodeId={}", defaultNodeId);
            if (defaultNodeId != null) {
                WfDefinitionLine defaultLine = outgoingLines.stream()
                        .filter(l -> l.getTargetNodeId().equals(defaultNodeId))
                        .findFirst()
                        .orElse(outgoingLines.get(0));
                log.info("默认连线: lineId={}, targetNodeId={}", defaultLine.getId(), defaultLine.getTargetNodeId());
                return defaultLine;
            }

            log.warn("没有满足的条件且没有 defaultNodeId，返回第一条连线");
        } catch (Exception e) {
            log.error("解析条件表达式失败: {}", conditionRule, e);
        }

        WfDefinitionLine firstLine = outgoingLines.get(0);
        log.info("最终选择: lineId={}, targetNodeId={}", firstLine.getId(), firstLine.getTargetNodeId());
        return firstLine;
    }

    /**
     * 简单表达式求值
     * 支持: amount > 10000, status == 'APPROVED', implApplyType == '0', etc.
     */
    private boolean evaluateExpression(WfEngineContext context, String expression) {
        try {
            // 简单实现：支持基本的比较运算
            // 实际生产环境建议使用规则引擎如Drools
            String expr = expression.trim();
            log.info("evaluateExpression 原始表达式: {}", expr);
            log.info("evaluateExpression 上下文变量: {}", context.getVariables());

            // 替换变量 - 使用正则确保完整单词匹配
            for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                String varName = entry.getKey();
                Object varValue = entry.getValue();
                if (varValue == null) {
                    expr = expr.replaceAll("\\b" + varName + "\\b", "null");
                } else if (varValue instanceof Number) {
                    // 数字类型直接替换
                    expr = expr.replaceAll("\\b" + varName + "\\b", varValue.toString());
                } else {
                    // 字符串类型需要加引号
                    expr = expr.replaceAll("\\b" + varName + "\\b", "'" + varValue.toString() + "'");
                }
            }
            log.info("evaluateExpression 替换后表达式: {}", expr);

            // 解析表达式
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                return Double.parseDouble(parts[0].trim()) >= Double.parseDouble(parts[1].trim());
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                return Double.parseDouble(parts[0].trim()) <= Double.parseDouble(parts[1].trim());
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                String left = unquote(parts[0].trim());
                String right = unquote(parts[1].trim());
                return !left.equals(right);
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                String left = unquote(parts[0].trim());
                String right = unquote(parts[1].trim());
                log.info("== 比较: left={}, right={}, result={}", left, right, left.equals(right));
                return left.equals(right);
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

    /**
     * 去除字符串字面量的引号
     */
    private String unquote(String value) {
        if (value == null) return null;
        value = value.trim();
        if ((value.startsWith("'") && value.endsWith("'")) ||
            (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
