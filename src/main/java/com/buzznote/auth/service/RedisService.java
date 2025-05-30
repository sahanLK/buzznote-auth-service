package com.buzznote.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


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
}
