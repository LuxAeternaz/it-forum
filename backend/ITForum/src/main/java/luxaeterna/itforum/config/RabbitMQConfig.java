package luxaeterna.itforum.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MODERATION_QUEUE = "moderation.queue";
    public static final String MODERATION_EXCHANGE = "moderation.exchange";
    public static final String MODERATION_ROUTING_KEY = "moderation";

    @Bean
    public Queue moderationQueue() {
        return QueueBuilder.durable(MODERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", MODERATION_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public TopicExchange moderationExchange() {
        return new TopicExchange(MODERATION_EXCHANGE);
    }

    @Bean
    public Binding moderationBinding() {
        return BindingBuilder.bind(moderationQueue()).to(moderationExchange()).with(MODERATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
