package my.test.websocketsketchbook.logic;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.test.websocketsketchbook.config.RabbitConfig;
import my.test.websocketsketchbook.model.ChatDto;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompRabbitController {

    private final RabbitTemplate template;
    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";

    @MessageMapping("chat.enter.{targetIuser}")
    public void enter(@Payload String message,  @DestinationVariable Long targetIuser) {
        ChatDto chat = new ChatDto();
        chat.setMessage("입장");
        chat.setRegDate(LocalDateTime.now());
        chat.setId(1L);
        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + targetIuser, chat); // use exchange
//        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic

    }

    @MessageMapping("chat.message.{targetIuser}")
    public void send(@Payload String message , @DestinationVariable Long targetIuser) {
        ChatDto chat = new ChatDto();
        chat.setRegDate(LocalDateTime.now());
        chat.setId(1L);
        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + targetIuser, chat); // use exchange
        //        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic
    }

    // receive 는 단순히 메시지를 소비 (출력) 한다. - 디버그 용
//    @RabbitListener(bindings = @QueueBinding(
//            exchange = @Exchange(name = CHAT_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
//            value = @Queue(name = CHAT_QUEUE_NAME),
//            key = "room.*"
//    ))
//    public void receive(String message) {
//        System.out.println("=========================================");
//        log.info("message = {}", message);
//    }

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.*"
    ))
    public void receive(String message) {
        System.out.println("=========================================");
        log.info("message = {}", message);
    }
}
