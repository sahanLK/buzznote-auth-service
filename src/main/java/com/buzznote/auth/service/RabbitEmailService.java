package com.buzznote.auth.service;

import com.buzznote.auth.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitEmailService {
    private final RabbitTemplate rabbitTemplate;

    private final RedisService redisService;

    private final SecureRandom random = new SecureRandom();

    public String generateToken() {
        return new BigInteger(130, random).toString(32);
    }

    public RabbitEmailService(RabbitTemplate rabbitTemplate, RedisService redisService) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisService = redisService;
    }

    public void sendPasswordResetEmail(String to) {
        Map<String, String> message = new HashMap<>();
        message.put("to", to);

        String token = this.generateToken();
        System.out.println("Generated Token: " + token);
        redisService.setPasswordResetToken(to, token);
        message.put("token", token);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}
