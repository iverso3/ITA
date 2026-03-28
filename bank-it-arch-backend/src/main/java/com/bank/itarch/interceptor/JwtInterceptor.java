package com.bank.itarch.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bank.itarch.common.Result;
import com.bank.itarch.config.JwtConfig;
import com.bank.itarch.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            sendUnauthorized(response, "未登录或Token已过期");
            return false;
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.getSubject();
            List<Long> roleIds = claims.get("roleIds", List.class);
            List<String> roleCodes = claims.get("roleCodes", List.class);

            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setUserId(userId);
            userInfo.setUsername(username);
            userInfo.setRoleIds(roleIds);
            userInfo.setRoleCodes(roleCodes);
            UserContext.setUser(userInfo);

            return true;
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
            sendUnauthorized(response, "Token验证失败或已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConfig.getHeaderName());
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(jwtConfig.getTokenPrefix())) {
            return bearerToken.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
