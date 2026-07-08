package luxaeterna.itforum.service;

import luxaeterna.itforum.entity.Comment;

public interface AiAgentService {
    void handleMention(Comment userComment);
}
