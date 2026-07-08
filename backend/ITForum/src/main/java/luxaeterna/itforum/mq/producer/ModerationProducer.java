package luxaeterna.itforum.mq.producer;

import luxaeterna.itforum.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModerationProducer {

    private final RabbitTemplate rabbitTemplate;

    public ModerationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendModerationTask(String targetType, Long targetId, Long userId, String content) {
        Map<String, Object> message = Map.of(
                "targetType", targetType,
                "targetId", targetId,
                "userId", userId,
                "content", content
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.MODERATION_EXCHANGE,
                RabbitMQConfig.MODERATION_ROUTING_KEY, message);
    }
}
