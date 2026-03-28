package com.bank.itarch.util;

import com.bank.itarch.model.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class UserContext {
    private static final ThreadLocal<UserInfo> userHolder = new ThreadLocal<>();

    public static void setUser(UserInfo user) {
        userHolder.set(user);
    }

    public static UserInfo getUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }

    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private List<Long> roleIds;
        private List<String> roleCodes;
    }
}
