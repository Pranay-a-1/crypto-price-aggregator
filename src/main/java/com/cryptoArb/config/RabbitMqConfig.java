package com.cryptoArb.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ messaging.
 * Defines the Exchange, Queue, and Binding for PriceTicks.
 */
@Configuration
public class RabbitMqConfig {

    @Value("${cpa.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${cpa.rabbitmq.queue}")
    private String queueName;

    @Value("${cpa.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
