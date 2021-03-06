package com.alexstrasza.currency.components;

import com.alexstrasza.currency.models.DataContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessenger
{
    @Value("${alexstrasza.rabbit.exchange}")
    private String directExchange;

    @Value("${alexstrasza.routing.webapi.currencyChange}")
    private String webapiRouting;

    @Value("${alexstrasza.routing.auction.bids}")
    private String returnBidsRouting;

//    @Value("${alexstrasza.routing.auction.buyout}")
//    private String returnBuyoutRouting;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void SendCurrencyUpdate(Integer item, String user, String change, String floating)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try
        {
            json = objectMapper.writeValueAsString(item);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }

        MessageProperties properties = new MessageProperties();
        properties.setHeader("userId", user);
        properties.setHeader("changeType", change);
        properties.setHeader("floating", floating);
        Message message = new Message(json.getBytes(), properties);
        rabbitTemplate.convertAndSend(directExchange, webapiRouting, message);
    }

//    public void ReplyBuyout(DataContainer amount, String auctionId, String user)
//    {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = "";
//
//        try
//        {
//            json = objectMapper.writeValueAsString(amount);
//        }
//        catch (JsonProcessingException e)
//        {
//            e.printStackTrace();
//        }
//
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("auctionId", auctionId);
//        properties.setHeader("user", user);
//        Message message = new Message(json.getBytes(), properties);
//        rabbitTemplate.convertAndSend(directExchange, returnBuyoutRouting, message);
//    }

    public void ReplyBid(DataContainer amount, String auctionId, String user)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";

        try
        {
            json = objectMapper.writeValueAsString(amount);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }

        MessageProperties properties = new MessageProperties();
        properties.setHeader("auctionId", auctionId);
        properties.setHeader("user", user);
        Message message = new Message(json.getBytes(), properties);
        rabbitTemplate.convertAndSend(directExchange, returnBidsRouting, message);
    }
}
