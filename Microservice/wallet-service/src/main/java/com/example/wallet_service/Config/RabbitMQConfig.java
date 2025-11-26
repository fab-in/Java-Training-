package com.example.wallet_service.Config;

import com.example.wallet_service.DTO.OtpVerifiedEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
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
     * Custom message converter that handles type mapping between services
     * Maps transaction_service DTOs to wallet_service DTOs
     */
    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        
        // Create a custom class mapper that maps transaction-service types to wallet-service types
        DefaultClassMapper classMapper = new DefaultClassMapper() {
            @Override
            public Class<?> toClass(MessageProperties properties) {
                // Get the type ID from message properties
                String typeId = (String) properties.getHeaders().get("__TypeId__");
                
                // Map transaction-service DTOs to wallet-service DTOs
                if (typeId != null) {
                    if (typeId.equals("com.example.transaction_service.DTO.OtpVerifiedEvent")) {
                        return OtpVerifiedEvent.class;
                    }
                }
                
                // Fall back to default behavior
                return super.toClass(properties);
            }
        };
        
        // Add trusted packages
        classMapper.setTrustedPackages(
            "com.example.transaction_service.DTO",
            "com.example.wallet_service.DTO"
        );
        
        converter.setClassMapper(classMapper);
        return converter;
    }
    
    /**
     * RabbitTemplate with JSON message converter
     * Converts Java objects to JSON automatically
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    /**
     * RabbitListenerContainerFactory with custom message converter
     * This ensures listeners can deserialize messages from transaction-service
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        
        // Configure to prevent infinite retries
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(1);
        
        return factory;
    }
}

