package com.buzznote.auth.service;

import com.buzznote.auth.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setAccessToken(String email, String accessToken) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        redisTemplate.opsForHash().putAll("USER:" + email, tokens);
    }

    public void setRefreshToken(String email, String refreshToken) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("refreshToken", refreshToken);
        redisTemplate.opsForHash().putAll("USER:" + email, tokens);
    }

    public boolean isValidAccessToken(String userId, String token) {
        return getAccessToken(userId).equals(token);
    }

    public boolean isValidRefreshToken(String userId, String token) {
        return getRefreshToken(userId).equals(token);
    }

    private String getAccessToken(String email) {
        return (String) redisTemplate.opsForHash().get("USER:" + email, "accessToken");
    }

    private String getRefreshToken(String email) {
        return (String) redisTemplate.opsForHash().get("USER:" + email, "refreshToken");
    }

    public void removeUser(String userId) {
        redisTemplate.delete("USER:" + userId);
    }

    public void setPasswordResetToken(String to, String token) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set("RESET_TOKEN:" + token, to, 15, TimeUnit.MINUTES);
    }

    public String getPasswordResetUserEmail(String token) {
        Object obj = redisTemplate.opsForValue().get("RESET_TOKEN:" + token);
        if (obj == null) {
            throw new InvalidTokenException("Reset token is invalid or expired");
        }
        return obj.toString();
    }

    public boolean validatePasswordResetToken(String token) {
        Object obj = redisTemplate.opsForValue().get("RESET_TOKEN:" + token);
        String value = obj != null ? obj.toString() : null;
        return value != null;
    }
}
