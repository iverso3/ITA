package com.bank.itarch.meta.util;

import java.util.regex.Pattern;

/**
 * SQL注入防护工具类
 * 提供SQL关键词检测和SQL安全验证方法
 */
public class MetaSQLUtil {

    // SQL注入常见恶意模式
    private static final String[] SQL_BLACKLIST = {
            "';", "--", "/*", "*/", "@@", "@",
            "char", "nchar", "varchar", "nvarchar",
            "alter", "begin", "cast", "create", "cursor",
            "declare", "delete", "drop", "end", "exec",
            "execute", "fetch", "insert", "kill", "select",
            "sys", "sysobjects", "syscolumns", "table",
            "update", "xp_cmdshell", "xp_", "sp_",
            "0x", "char(", "nchar(", "varchar("
    };

    // SQL关键词正则模式（用于检测）
    private static final Pattern[] SQL_PATTERNS = {
            Pattern.compile("(?i)(union.*select|select.*from|insert.*into|delete.*from|update.*set|drop.*table|create.*table)"),
            Pattern.compile("(?i)(or\\s+\\d+\\s*=\\s*\\d+|and\\s+\\d+\\s*=\\s*\\d+)"),
            Pattern.compile("(?i)(exec\\s*\\(|execute\\s*\\(|eval\\s*\\()"),
            Pattern.compile("(?i)(script|<script|javascript:|onerror=|onclick=)"),
            Pattern.compile("(?i)(union\\s+all|union\\s+select)"),
            Pattern.compile("(?i)(sleep\\s*\\(|benchmark\\s*\\()"),
            Pattern.compile("(?i)(load_file|into\\s+outfile|dumpfile)"),
            Pattern.compile("(?i)(information_schema|performance_schema|mysql|pg_catalog)"),
            Pattern.compile("(?i)(--\\s*$|#\\s*$)"),
            Pattern.compile("(?i)(;\\s*--\\s*$)")
    };

    // 数字型SQL注入检测
    private static final Pattern NUMERIC_SQL_PATTERN = Pattern.compile("^-?\\d+$");

    private MetaSQLUtil() {
        // 私有构造函数
    }

    /**
     * 检查是否包含SQL注入风险
     *
     * @param input 待检查的字符串
     * @return true表示安全，false表示存在风险
     */
    public static boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        String lowerInput = input.toLowerCase().trim();

        // 检查黑名单关键词
        for (String keyword : SQL_BLACKLIST) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                return false;
            }
        }

        // 检查正则模式
        for (Pattern pattern : SQL_PATTERNS) {
            if (pattern.matcher(lowerInput).find()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证字符串是否安全，如果存在风险则抛出异常
     *
     * @param input     待验证的字符串
     * @param fieldName 字段名称（用于错误提示）
     * @throws IllegalArgumentException 当检测到SQL注入风险时抛出
     */
    public static void validateSafe(String input, String fieldName) {
        if (!isSafe(input)) {
            throw new IllegalArgumentException("检测到 " + fieldName + " 存在安全风险，请勿输入非法字符");
        }
    }

    /**
     * 安全转义SQL字符串（用于LIKE查询）
     *
     * @param input 原始字符串
     * @return 转义后的字符串
     */
    public static String escapeForLike(String input) {
        if (input == null) {
            return null;
        }
        // 转义特殊字符：\ -> \\, % -> \%, _ -> \_, [ -> \[
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }

    /**
     * 清理字符串，移除可能的SQL注入攻击
     *
     * @param input 原始输入
     * @return 清理后的安全字符串
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // 移除常见的注入攻击模式
        result = result.replaceAll("(?i);\\s*--\\s*$", ""); // 移除行尾的SQL注释
        result = result.replaceAll("(?i)/\\*.*\\*/", "");   // 移除块注释
        result = result.replaceAll("(?i)--\\s*\\w+\\s*$", ""); // 移除行注释

        // 移除多余的分号
        result = result.replaceAll(";+", ";");

        // 如果清理后为空，返回原值（让isSafe进一步判断）
        if (result.trim().isEmpty()) {
            return input;
        }

        return result;
    }

    /**
     * 验证参数是否为安全的数字
     *
     * @param value 待验证的值
     * @return 安全返回原值，否则返回null
     */
    public static Long safeParseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // 纯数字才允许
        if (NUMERIC_SQL_PATTERN.matcher(value.trim()).matches()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 验证参数是否为安全的整数
     *
     * @param value 待验证的值
     * @return 安全返回原值，否则返回null
     */
    public static Integer safeParseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (NUMERIC_SQL_PATTERN.matcher(value.trim()).matches()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查是否为安全的标识符（表名、字段名等）
     *
     * @param identifier 标识符
     * @return true表示安全
     */
    public static boolean isSafeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        // 只允许字母、数字、下划线，且不以数字开头
        return identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    /**
     * 安全构建标识符列表（用于IN查询）
     *
     * @param identifiers 标识符数组
     * @return 安全的标识符字符串数组
     */
    public static String[] safeIdentifiers(String... identifiers) {
        if (identifiers == null || identifiers.length == 0) {
            return new String[0];
        }
        String[] result = new String[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            if (isSafeIdentifier(identifiers[i])) {
                result[i] = identifiers[i];
            } else {
                result[i] = null; // 不安全的标识符返回null
            }
        }
        return result;
    }
}
