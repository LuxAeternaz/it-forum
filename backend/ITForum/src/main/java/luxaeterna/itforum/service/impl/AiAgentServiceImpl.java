package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.ai.ForumAiAgent;
import luxaeterna.itforum.entity.Agent;
import luxaeterna.itforum.entity.AiAgentResponse;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.mapper.AiAgentResponseMapper;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.service.AgentService;
import luxaeterna.itforum.service.AiAgentService;
import luxaeterna.itforum.service.NotificationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AiAgentServiceImpl implements AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);
    private static final long BOT_USER_ID = 1L;

    private final ForumAiAgent forumAiAgent;
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final AiAgentResponseMapper aiAgentResponseMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final AgentService agentService;
    private final NotificationService notificationService;

    public AiAgentServiceImpl(ForumAiAgent forumAiAgent, CommentMapper commentMapper,
                               PostMapper postMapper, AiAgentResponseMapper aiAgentResponseMapper,
                               SnowflakeIdGenerator idGenerator, AgentService agentService,
                               NotificationService notificationService) {
        this.forumAiAgent = forumAiAgent;
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.aiAgentResponseMapper = aiAgentResponseMapper;
        this.idGenerator = idGenerator;
        this.agentService = agentService;
        this.notificationService = notificationService;
    }

    @Override
    @Async
    public void handleMention(Comment userComment) {
        String content = userComment.getContent();
        Agent agent = detectAgent(content);
        if (agent == null) {
            return;
        }

        try {
            String atName = "@" + agent.getName();
            String prompt = content.substring(content.indexOf(atName) + atName.length()).trim();
            if (prompt.isEmpty()) {
                prompt = "你好，请介绍一下自己";
            }

            Post post = postMapper.selectById(userComment.getPostId());
            String postContext = post != null ? "标题: " + post.getTitle() + "\n" + truncate(post.getContent(), 500) : "";

            String threadContext = "";
            if (userComment.getParentId() != null) {
                Comment parent = commentMapper.selectById(userComment.getParentId());
                if (parent != null) {
                    threadContext = "回复给: " + parent.getContent();
                }
            }

            String response = forumAiAgent.respond(agent.getName(), agent.getPersona(), postContext, threadContext, prompt);

            Comment aiComment = new Comment();
            aiComment.setId(idGenerator.nextId());
            aiComment.setContent("**[@" + agent.getName() + "]** " + response);
            aiComment.setUserId(BOT_USER_ID);
            aiComment.setPostId(userComment.getPostId());
            aiComment.setParentId(userComment.getId());
            aiComment.setRootId(userComment.getRootId() != null ? userComment.getRootId() : userComment.getId());
            aiComment.setReplyToUserId(userComment.getUserId());
            aiComment.setStatus("PUBLISHED");
            commentMapper.insert(aiComment);

            AiAgentResponse record = new AiAgentResponse();
            record.setId(idGenerator.nextId());
            record.setTriggerUserId(userComment.getUserId());
            record.setCommentId(aiComment.getId());
            record.setPostId(userComment.getPostId());
            record.setParentCommentId(userComment.getId());
            record.setAgentId(agent.getId());
            record.setPrompt(prompt);
            record.setResponse(response);
            record.setModelUsed("gpt-4o");
            aiAgentResponseMapper.insert(record);

            // Notify the user who triggered the agent
            notificationService.createNotification(userComment.getUserId(), "MENTION", BOT_USER_ID,
                "@" + agent.getName() + " 回复了你", truncate(response, 100),
                "POST", userComment.getPostId());

            // Also notify post author if different from the trigger user
            if (post != null && !post.getUserId().equals(userComment.getUserId())) {
                notificationService.createNotification(post.getUserId(), "REPLY", BOT_USER_ID,
                    "@" + agent.getName() + " 回复了帖子", truncate(response, 100),
                    "POST", userComment.getPostId());
            }
        } catch (Exception e) {
            log.error("AI agent response failed", e);
        }
    }

    private Agent detectAgent(String content) {
        List<Agent> agents = agentService.listAllActive();
        agents.sort(Comparator.comparingInt((Agent a) -> a.getName().length()).reversed());
        for (Agent agent : agents) {
            if (content.contains("@" + agent.getName())) {
                return agent;
            }
        }
        return null;
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }
}
