package my.test.websocketsketchbook.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.StringMessageConverter;

@Configuration
@EnableRabbit
public class RabbitConfig {

    //맘데로 해도되지만 현민씨가 정해놓기로함 보안상 이름우리가짓는게 좋음
    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    public static final String ROUTING_KEY = "room"; // 채팅방 찾아가도록 하는 기능 DEFAULT

    @Bean
    public Queue testQueue() {
        return new Queue("test", true); // true는 durable 속성으로서, 큐를 메모리가 아닌 디스크에 저장하도록 하는 옵션
    }

    @Bean
    public Queue queue() {
        return new Queue(CHAT_QUEUE_NAME, true); // 서버를 끌 때 Queue를 제거되지않도록 true
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(CHAT_EXCHANGE_NAME); // Topic 사용
    }

    @Bean // 바인딩 Queue를 to exchange
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean // 컨버터
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory()); // 로그인정보 담겨있는거 사용선언
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter()); //메세지 컨버터 세팅된거 선언
        rabbitTemplate.setRoutingKey(CHAT_QUEUE_NAME);
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer container() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(CHAT_QUEUE_NAME);
        return container;
    }

    // Spring 기본 커넥션 펙토리를 새로운 커넥션 팩토리로 변경
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    @Bean // 이걸 레빗템플릿에 끼워넣는다고함..? 일단 이거그대로 사용
    public StringMessageConverter stringMessageConverter() {
        // String 으로 변환하는 컨버터 - 가장 문제가 적음.
        return new StringMessageConverter();
    }

    @Bean
    public Module dateTimeModule() {
        return new JavaTimeModule();
    }




}
