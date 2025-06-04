package com.buzznote.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String PASSWORD_RESET_EMAIL_QUEUE = "email.password-reset.queue";
    public static final String PASSWORD_RESET_EMAIL_EXCHANGE = "email.password-reset.exchange";
    public static final String PASSWORD_RESET_EMAIL_ROUTING_KEY = "email.password-reset";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public Queue passwordResetEmailQueue() {
        return new Queue(PASSWORD_RESET_EMAIL_QUEUE);
    }

    @Bean
    public DirectExchange passwordResetEmailExchange() {
        return new DirectExchange(PASSWORD_RESET_EMAIL_EXCHANGE);
    }

    @Bean
    public Binding bindingPasswordResetEmailQueue() {
        return BindingBuilder.bind(passwordResetEmailQueue()).to(passwordResetEmailExchange()).with(PASSWORD_RESET_EMAIL_ROUTING_KEY);
    }

}
