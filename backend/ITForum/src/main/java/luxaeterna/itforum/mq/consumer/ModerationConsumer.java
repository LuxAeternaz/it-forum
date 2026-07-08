package luxaeterna.itforum.mq.consumer;

import luxaeterna.itforum.config.RabbitMQConfig;
import luxaeterna.itforum.service.AiModerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModerationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ModerationConsumer.class);

    private final AiModerationService aiModerationService;

    public ModerationConsumer(AiModerationService aiModerationService) {
        this.aiModerationService = aiModerationService;
    }

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConfig.MODERATION_QUEUE)
    public void handleModerationTask(Map<String, Object> message) {
        try {
            String targetType = (String) message.get("targetType");
            Long targetId = ((Number) message.get("targetId")).longValue();
            Long userId = ((Number) message.get("userId")).longValue();
            String content = (String) message.get("content");

            log.info("Processing moderation for {}:{}", targetType, targetId);
            aiModerationService.moderate(targetType, targetId, userId, content);
        } catch (Exception e) {
            log.error("Failed to process moderation task", e);
        }
    }
}
