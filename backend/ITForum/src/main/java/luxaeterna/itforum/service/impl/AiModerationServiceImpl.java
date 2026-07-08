package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.ai.ModeratorAgent;
import luxaeterna.itforum.entity.ModerationLog;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.ModerationLogMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.service.AiModerationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiModerationServiceImpl implements AiModerationService {

    private static final Logger log = LoggerFactory.getLogger(AiModerationServiceImpl.class);

    private final ModeratorAgent moderatorAgent;
    private final ModerationLogMapper moderationLogMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final SnowflakeIdGenerator idGenerator;

    public AiModerationServiceImpl(ModeratorAgent moderatorAgent, ModerationLogMapper moderationLogMapper,
                                    PostMapper postMapper, CommentMapper commentMapper, SnowflakeIdGenerator idGenerator) {
        this.moderatorAgent = moderatorAgent;
        this.moderationLogMapper = moderationLogMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public void moderate(String targetType, Long targetId, Long userId, String content) {
        try {
            String snippet = content.length() > 500 ? content.substring(0, 500) : content;
            Map<String, Object> result = moderatorAgent.evaluate(content);

            ModerationLog log = new ModerationLog();
            log.setId(idGenerator.nextId());
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setUserId(userId);
            log.setContentSnippet(snippet);
            log.setResult((String) result.get("result"));
            log.setReason((String) result.get("reason"));
            if (result.get("confidence") != null) {
                log.setConfidence(new java.math.BigDecimal(result.get("confidence").toString()));
            }
            moderationLogMapper.insert(log);

            String modResult = (String) result.get("result");
            if ("REJECTED".equals(modResult)) {
                if ("POST".equals(targetType)) {
                    postMapper.updateStatus(targetId, "REMOVED");
                } else if ("COMMENT".equals(targetType)) {
                    commentMapper.updateStatus(targetId, "REMOVED");
                }
            } else if ("FLAGGED".equals(modResult)) {
                if ("POST".equals(targetType)) {
                    postMapper.updateStatus(targetId, "FLAGGED");
                } else if ("COMMENT".equals(targetType)) {
                    commentMapper.updateStatus(targetId, "FLAGGED");
                }
            }
        } catch (Exception e) {
            log.error("AI moderation failed for {}:{}", targetType, targetId, e);
        }
    }
}
