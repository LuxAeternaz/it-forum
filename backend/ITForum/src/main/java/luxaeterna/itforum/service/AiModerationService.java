package luxaeterna.itforum.service;

public interface AiModerationService {
    void moderate(String targetType, Long targetId, Long userId, String content);
}
