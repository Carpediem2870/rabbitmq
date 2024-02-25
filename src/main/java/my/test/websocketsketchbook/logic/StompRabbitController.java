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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompRabbitController {

    private final RabbitTemplate template; // RabbitConfig에서 등록된것들 DI
    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange"; // queue를 찾아서 도착지를 바꿈
//    private final org.springframework.amqp.core.Exchange exchange;
//    private final Binding binding;
    @Autowired
    NewDtoRepository

    /*
    *  FIXME -> routingKey 를 각 채팅방 별로 지정하고, 저장할 수 있게 컬럼을 추가해주자 그리고 targetIuser 가 제공되면,
    *           로그인 유져와 target 유저가 속해있는 방의 저장되어있는 routingKey 를 가져와서 해당키를 queue 방식으로 구독시키자.
    */
    // TODO -> 현재는 routingKey 자체가 Queue 가 되고, 자기 자신으로 라우팅 한다.
    // TODO -> 1:1채팅은 무조건 큐 발급, 큐 구독으로 하자 (routingKey 는 쓰지않고, 큐만으로 구분을 하자.)
    // TODO -> 이제 큐구독을 어떻게 하는지를 알아보아야 한다.
    // TODO -> 리스너로는 테스트 성공한다.
    //
    /*
        TODO -> 만약 프론트에서 구독을 수행한다면, ws://localhost:8080/ws 로 소켓 연결,
                /chat/message.{targetIuser} 로 요청하면 해당 유저가 대상유저와 채팅하기위해 정해진 queue 이름으로 메시지를 보낼수 있다는것을
                말해주어야 한다.
                다만, 이사람이 메시지를 받는 @RabbitListener(queues = "xxx") 처럼 내가 큐 이름을 리턴해주어야 한다면
                사전에 @GetMapping 으로 targetIuser 정보를 보내면 해당 로그인유저와 target 유저의 채팅을 위한 구독 큐 를 알려주도록 하자.
                큐는 chat_user 테이블의 두 유저로 인해 생성된 pk 를 통해 만들자.

        TODO -> 실제로 구현시에는 @GetMapping 으로 queue 정보 제공 ->
                @MessageMapping 에 경로(/chat/message.{queue} 에 요청, 구독 할 수 있도록) 를 알려줌
                그 이후, 클라이언트는 /chat/message.{queue} 로 소켓요청 -> 이때 메시지를 포함해서 보내야 함 그 메시지가 전송됨
    */

    //RabbitMq 큐를 생성하고 테스트메세지를 보내는 메소드
    @GetMapping("queue")
    public void createQueue(@RequestParam(required = false) String routingKey) {
//        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + routingKey, "testMessage");
        template.convertAndSend("test" + (routingKey == null ? "" : routingKey), "testMessage");
    }

    @RabbitListener(queues = "test")
    public void receive3(String message) {
        System.out.println("==================== receive3 :: use Queue =====================");
        log.info("message = {}", message);
    }
//    @RabbitListener(queues = "test1")
//    public void receive1(String message) {
//        System.out.println("==================== receive3 :: use Queue =====================");
//        log.info("message = {}", message);
//    }
//    @RabbitListener(queues = "test2")
//    public void receive4(String message) {
//        System.out.println("==================== receive3 :: use Queue =====================");
//        log.info("message = {}", message);
//    }
//    @RabbitListener(queues = "test3")
//    public void receive5(String message) {
//        System.out.println("==================== receive3 :: use Queue =====================");
//        log.info("message = {}", message);
//    }
//    @RabbitListener(queues = "test4")
//    public void receive6(String message) {
//        System.out.println("==================== receive3 :: use Queue =====================");
//        log.info("message = {}", message);
//    }
//    @RabbitListener(queues = "test5")
//    public void receive7(String message) {
//        System.out.println("==================== receive3 :: use Queue =====================");
//        log.info("message = {}", message);
//    }

    /* ----------------------------------------------------------------------- */

    /*@MessageMapping("enter.{targetIuser}")
    public void enter(@Payload String message, @DestinationVariable Long targetIuser) {
        ChatDto chat = new ChatDto();
        chat.setMessage("입장");
        chat.setRegDate(LocalDateTime.now());
        chat.setId(1L);
        template.convertAndSend(CHAT_EXCHANGE_NAME, String.valueOf(targetIuser), chat); // use exchange
//        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic
    }*/

    @MessageMapping("message.{targetIuser}")
    public void send(@Payload String message, @DestinationVariable Long targetIuser) {

        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + targetIuser, message); // chat 은 chat PK
        //        template.convertAndSend("room." + targetIuser, chat); // use queue
//        template.convertAndSend("amq.topic", "room." + targetIuser, chat); // topic


        //여기에서 DB에 저장하도록 설계하면됨
    }

    @MessageMapping("chat.room.send.{ireceiver}")
    public void send2(@Payload String message, @DestinationVariable Long ireceiver) {
        int ichat = 1;
        template.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + ichat, message);
        // insert into t_chat (xx, x, x, msg) (y, yy, y, #{message})
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


    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.1"
    ))
    public void receive1(String message) {
        System.out.println("====================1=====================");
        log.info("message = {}", message);
    }

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.2"
    ))
    public void receive2(String message) {
        System.out.println("=====================2====================");
        log.info("message = {}", message);
    }
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(name = CHAT_EXCHANGE_NAME),
            value = @Queue(name = CHAT_QUEUE_NAME),
            key = "room.3"
    ))
    public void receive33(String message) {
        System.out.println("====================3=====================");
        log.info("message = {}", message);
    }



//    @EventListener(ApplicationReadyEvent.class)
//    public void init() {
//        System.out.println(binding.getDestination());
//        System.out.println(binding.getDestinationType());
//    }


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
