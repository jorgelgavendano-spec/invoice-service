package com.empresa.invoice_service.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration 
public class RabbitMQConfig  {
    public static final String COLA_TIMBRADO = "cola.timbrado";
    public static final String EXCHANGE_TIMBRADO = "exchange.timbrado";
    public static final String ROUTING_KEY_TIMBRADO = "timbrado.solicitud";

    @Bean
    public Queue colaTimbrado() {
        return QueueBuilder.durable(COLA_TIMBRADO).build();
    }

    @Bean
    public DirectExchange exchangeTimbrado() {
        return new DirectExchange(EXCHANGE_TIMBRADO);
    }

    @Bean
    public Binding bindingTimbrado(Queue colaTimbrado, DirectExchange exchangeTimbrado) {
        return BindingBuilder.bind(colaTimbrado)
                .to(exchangeTimbrado)
                .with(ROUTING_KEY_TIMBRADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
}
