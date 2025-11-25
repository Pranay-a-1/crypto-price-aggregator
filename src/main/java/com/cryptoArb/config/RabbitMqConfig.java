package com.cryptoArb.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Phase 14: Enhanced RabbitMQ Configuration with Retry and Dead Letter Queue.
 * 
 * This configuration provides:
 * 1. Main queue with dead letter exchange for failed messages
 * 2. Dead letter queue to capture permanently failed messages
 * 3. Retry policy with max 3 attempts and exponential backoff
 * 4. Message recovery to DLQ after max retries
 */
@Configuration
public class RabbitMqConfig {

    @Value("${cpa.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${cpa.rabbitmq.queue}")
    private String queueName;

    @Value("${cpa.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${cpa.rabbitmq.dlq.exchange:price.tick.dlx}")
    private String dlxName;

    @Value("${cpa.rabbitmq.dlq.queue:price.tick.dlq}")
    private String dlqName;

    @Value("${cpa.rabbitmq.dlq.routing-key:price.tick.dlq.key}")
    private String dlqRoutingKey;

    // --- Main Exchange and Queue ---

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * Main queue with dead letter exchange configuration.
     * Messages that fail after max retries will be sent to the DLX.
     */
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    // --- Dead Letter Exchange and Queue ---

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(dlxName);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(dlqRoutingKey);
    }

    // --- Message Converter ---

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- Retry Configuration ---

    /**
     * Custom listener container factory with retry configuration.
     * 
     * Retry Policy:
     * - Max 3 attempts to process a message
     * - Exponential backoff: 1s, 2s, 4s
     * - After max retries, message will be rejected and sent to DLQ (via queue's
     * DLX config)
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // Set retry template - after max retries, message will be rejected
        // The queue's x-dead-letter-exchange config will route it to DLQ
        factory.setRetryTemplate(retryTemplate());

        // IMPORTANT: Set defaultRequeueRejected to false
        // This ensures that after max retries, the message is REJECTED (not requeued)
        // and routed to the DLX/DLQ
        factory.setDefaultRequeueRejected(false);

        return factory;
    }

    /**
     * Retry template with exponential backoff.
     * 
     * - Initial interval: 1000ms (1 second)
     * - Multiplier: 2.0 (doubles each time)
     * - Max interval: 10000ms (10 seconds)
     * - Max attempts: 3
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Retry policy: max 3 attempts
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Backoff policy: exponential (1s, 2s, 4s)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
