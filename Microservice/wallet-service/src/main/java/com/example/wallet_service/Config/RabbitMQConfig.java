package com.example.wallet_service.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Wallet Service
 * Defines queues and message converters for async communication
 */
@Configuration
public class RabbitMQConfig {
    
    // Queue names - must match Transaction Service
    public static final String TRANSACTION_CREATED_QUEUE = "transaction.created";
    public static final String OTP_VERIFIED_QUEUE = "otp.verified";
    public static final String TRANSACTION_COMPLETED_QUEUE = "transaction.completed";
    
    /**
     * Queue for publishing transaction creation events
     * Wallet Service publishes here, Transaction Service consumes
     */
    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue(TRANSACTION_CREATED_QUEUE, true); // true = durable
    }
    
    /**
     * Queue for consuming OTP verification events
     * Transaction Service publishes here, Wallet Service consumes
     */
    @Bean
    public Queue otpVerifiedQueue() {
        return new Queue(OTP_VERIFIED_QUEUE, true);
    }
    
    /**
     * Queue for consuming transaction completion events
     * Wallet Service publishes here, Transaction Service consumes
     */
    @Bean
    public Queue transactionCompletedQueue() {
        return new Queue(TRANSACTION_COMPLETED_QUEUE, true);
    }
    
    /**
     * RabbitTemplate with JSON message converter
     * Converts Java objects to JSON automatically
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}

