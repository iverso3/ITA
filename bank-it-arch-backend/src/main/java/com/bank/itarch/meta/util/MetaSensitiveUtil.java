package com.bank.itarch.meta.util;

import cn.hutool.core.util.DesensitizedUtil;

/**
 * 敏感数据脱敏工具类
 * 提供各类敏感信息的脱敏处理方法
 */
public class MetaSensitiveUtil {

    private MetaSensitiveUtil() {
        // 私有构造函数，禁止实例化
    }

    /**
     * 手机号脱敏
     * 示例: 13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        try {
            return DesensitizedUtil.mobilePhone(phone);
        } catch (Exception e) {
            // 如果 Hutool 脱敏失败，使用手动脱敏
            if (phone.length() == 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
            return "****";
        }
    }

    /**
     * 身份证号脱敏
     * 示例: 430123199001011234 -> 430***********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        try {
            return DesensitizedUtil.idCardNum(idCard, 4, 4);
        } catch (Exception e) {
            if (idCard.length() >= 18) {
                return idCard.substring(0, 6) + "********" + idCard.substring(14);
            }
            return "****";
        }
    }

    /**
     * 邮箱脱敏
     * 示例: test@example.com -> t***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        try {
            return DesensitizedUtil.email(email);
        } catch (Exception e) {
            int atIndex = email.indexOf("@");
            if (atIndex > 2) {
                return email.substring(0, 2) + "***" + email.substring(atIndex);
            } else if (atIndex > 0) {
                return email.substring(0, 1) + "***" + email.substring(atIndex);
            }
            return "***";
        }
    }

    /**
     * 银行卡脱敏
     * 示例: 6222021234567890123 -> 6222**********0123
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return bankCard;
        }
        try {
            return DesensitizedUtil.bankCard(bankCard);
        } catch (Exception e) {
            if (bankCard.length() >= 8) {
                return bankCard.substring(0, 4) + "********" + bankCard.substring(bankCard.length() - 4);
            }
            return "****";
        }
    }

    /**
     * 姓名脱敏
     * 示例: 张三 -> 张*  李明 -> 李*
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        try {
            return DesensitizedUtil.chineseName(name);
        } catch (Exception e) {
            if (name.length() == 2) {
                return name.substring(0, 1) + "*";
            } else if (name.length() > 2) {
                return name.substring(0, 1) + "*" + name.substring(name.length() - 1);
            }
            return "*";
        }
    }

    /**
     * 地址脱敏
     * 示例: 广东省深圳市南山区科技园xxx -> 广东省深圳市南山区科技园***
     */
    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        int length = address.length();
        if (length <= 6) {
            return "***";
        }
        return address.substring(0, length - 3) + "***";
    }

    /**
     * 密码脱敏
     * 返回固定长度的星号
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "******";
    }

    /**
     * IP地址脱敏
     * 示例: 192.168.1.100 -> 192.168.***.***
     */
    public static String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        try {
            return DesensitizedUtil.ipv4(ip);
        } catch (Exception e) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***.***";
            }
            return "***";
        }
    }

    /**
     * 信用卡脱敏（与银行卡相同）
     */
    public static String maskCreditCard(String cardNumber) {
        return maskBankCard(cardNumber);
    }

    /**
     * 用户名脱敏
     * 示例: admin -> a****n
     */
    public static String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return username;
        }
        int length = username.length();
        if (length <= 2) {
            return username.charAt(0) + "***";
        }
        return username.charAt(0) + "***" + username.charAt(length - 1);
    }

    /**
     * 自定义正则脱敏
     *
     * @param input   原始字符串
     * @param regex   正则表达式
     * @param replace 替换字符串
     * @return 脱敏后的字符串
     */
    public static String maskByRegex(String input, String regex, String replace) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            return input.replaceAll(regex, replace);
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * 自定义脱敏（指定保留前缀和后缀长度）
     *
     * @param input        原始字符串
     * @param prefixLength 保留前缀长度
     * @param suffixLength 保留后缀长度
     * @return 脱敏后的字符串
     */
    public static String maskCustom(String input, int prefixLength, int suffixLength) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        int length = input.length();
        if (length <= prefixLength + suffixLength) {
            return "*".repeat(Math.max(1, length));
        }

        StringBuilder result = new StringBuilder();
        result.append(input.substring(0, prefixLength));
        int maskLength = length - prefixLength - suffixLength;
        result.append("*".repeat(Math.max(1, maskLength)));
        result.append(input.substring(length - suffixLength));
        return result.toString();
    }

    /**
     * 通用脱敏方法（根据脱敏类型自动选择脱敏方式）
     *
     * @param value      原始值
     * @param fieldType  字段类型（PHONE/ID_CARD/EMAIL/BANK_CARD/NAME/ADDRESS/IP/PASSWORD）
     * @return 脱敏后的值
     */
    public static String mask(String value, String fieldType) {
        if (value == null || value.isEmpty() || fieldType == null || fieldType.isEmpty()) {
            return value;
        }

        switch (fieldType.toUpperCase()) {
            case "PHONE":
                return maskPhone(value);
            case "ID_CARD":
                return maskIdCard(value);
            case "EMAIL":
                return maskEmail(value);
            case "BANK_CARD":
                return maskBankCard(value);
            case "NAME":
                return maskName(value);
            case "ADDRESS":
                return maskAddress(value);
            case "PASSWORD":
                return maskPassword(value);
            case "IP":
            case "IP_ADDRESS":
                return maskIpAddress(value);
            case "USERNAME":
                return maskUsername(value);
            case "CREDIT_CARD":
                return maskCreditCard(value);
            default:
                // 未知类型，默认脱敏中间部分
                return maskCustom(value, 2, 2);
        }
    }
}
