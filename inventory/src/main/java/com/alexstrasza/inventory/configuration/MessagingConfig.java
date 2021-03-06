package com.alexstrasza.inventory.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig
{
    @Value("${alexstrasza.rabbit.exchange}")
    private String directExchange;

    @Value("${alexstrasza.queue.inventory.auctionProcessing}")
    private String auctionProcessingQueue;

    @Value("${alexstrasza.routing.inventory.auctionProcessing}")
    private String auctionProcessingRouting;

    @Value("${alexstrasza.queue.inventory.userCreate}")

    private String userCreationQueue;
    @Value("${alexstrasza.routing.inventory.userCreate}")
    private String userCreationRouting;

    @Bean
    public DirectExchange directExchange()
    {
        return new DirectExchange(directExchange, false, false);
    }

    @Bean
    public Queue auctionProcessingQueue()
    {
        return new Queue(auctionProcessingQueue);
    }

    @Bean
    public Queue userCreationQueue()
    {
        return new Queue(userCreationQueue);
    }

    @Bean
    public Binding auctionProcessingBinding(Queue auctionProcessingQueue, DirectExchange directExchange)
    {
        return BindingBuilder.bind(auctionProcessingQueue).to(directExchange).with(auctionProcessingRouting);
    }

    @Bean
    public Binding userCreationBinding(Queue userCreationQueue, DirectExchange directExchange)
    {
        return BindingBuilder.bind(userCreationQueue).to(directExchange).with(userCreationRouting);
    }
}
