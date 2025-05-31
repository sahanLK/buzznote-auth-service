package com.buzznote.auth.service;

import com.buzznote.auth.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitEmailService {
    private final RabbitTemplate rabbitTemplate;

    public RabbitEmailService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPasswordResetEmail(String to, String token) {
        Map<String, String> message = new HashMap<>();
        message.put("to", to);
        message.put("token", token);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}
