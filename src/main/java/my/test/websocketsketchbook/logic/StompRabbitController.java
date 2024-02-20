package my.test.websocketsketchbook.logic;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.test.websocketsketchbook.model.ChatDto;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompRabbitController {

    private final RabbitTemplate template;
    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final org.springframework.amqp.core.Exchange exchange;
    private final Binding binding;

    // TODO -> 1:1채팅은 무조건 큐 발급, 큐 구독으로 하자 (routingKey 는 쓰지않고, 큐만으로 구분을 하자.)
    // TODO -> 이제 큐구독을 어떻게 하는지를 알아보아야 한다.
    // TODO -> 리스너로는 테스트 성공한다.
    @GetMapping("queue")
    public void createQueue() {
//        template.convertAndSend(CHAT_EXCHANGE_NAME, "test", "testMessage");
        template.convertAndSend("test", "testMessage");
    }

    @RabbitListener(queues = "test")
    public void receive3(String message) {
        System.out.println("==================== receive3 :: use Queue =====================");
        log.info("message = {}", message);
    }

    /* ----------------------------------------------------------------------- */

    @MessageMapping("chat.enter.{targetIuser}")
    public void enter(@Payload String message, @DestinationVariable Long targetIuser) {
        ChatDto chat = new ChatDto();
        chat.setMessage("입장");
        chat.setRegDate(LocalDateTime.now());
        chat.setId(1L);
        template.convertAndSend(CHAT_EXCHANGE_NAME, String.valueOf(targetIuser), chat); // use exchange
//        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic

    }

    @MessageMapping("chat.message.{targetIuser}")
    public void send(@Payload String message, @DestinationVariable Long targetIuser) {
        ChatDto chat = new ChatDto();
        chat.setRegDate(LocalDateTime.now());
        chat.setId(1L);
        template.convertAndSend(CHAT_EXCHANGE_NAME, String.valueOf(targetIuser), chat); // use exchange
        //        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic
    }

    // receive 는 단순히 메시지를 소비 (출력) 한다. - 디버그 용
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.*"
    ))
    public void receive(String message) {
        System.out.println("=========================================");
        log.info("message = {}", message);
    }

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.*"
    ))
    public void receive2(String message) {
        System.out.println("=========================================");
        log.info("message = {}", message);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println(binding.getDestination());
        System.out.println(binding.getDestinationType());
    }


//    // 큐기준으로는 문제 없이 받아진다.
//    // 하지만, routingKey 이 서로 다르면 다른 routingKey 를 구독중인 사람은 해당 메시지를 받을수 없는것인지는 확인이 불가하다.
//    // 또한 routingKey 를 지정하는건 누가할까? => 프론트 ? 백 ?
//    // 구독은 누가? => 프론트 ? 백 ?
//    @RabbitListener(queues = CHAT_QUEUE_NAME)
//    public void receive2(String message) {
//        System.out.println("====================receive2=====================");
//        log.info("message = {}", message);
//    }


}
