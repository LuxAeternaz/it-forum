package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.dto.request.CreateCommentRequest;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.User;
import luxaeterna.itforum.entity.es.CommentDocument;
import luxaeterna.itforum.exception.BusinessException;
import luxaeterna.itforum.exception.ResourceNotFoundException;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.mapper.UserMapper;
import luxaeterna.itforum.mq.producer.ModerationProducer;
import luxaeterna.itforum.repository.es.CommentSearchRepository;
import luxaeterna.itforum.service.AiAgentService;
import luxaeterna.itforum.service.CommentService;
import luxaeterna.itforum.service.NotificationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    private static final long BOT_USER_ID = 1L;

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final AiAgentService aiAgentService;
    private final ModerationProducer moderationProducer;
    private final NotificationService notificationService;
    private final CommentSearchRepository commentSearchRepo;

    public CommentServiceImpl(CommentMapper commentMapper, PostMapper postMapper,
                              UserMapper userMapper,
                              SnowflakeIdGenerator idGenerator,
                              AiAgentService aiAgentService,
                              ModerationProducer moderationProducer,
                              NotificationService notificationService,
                              CommentSearchRepository commentSearchRepo) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.idGenerator = idGenerator;
        this.aiAgentService = aiAgentService;
        this.moderationProducer = moderationProducer;
        this.notificationService = notificationService;
        this.commentSearchRepo = commentSearchRepo;
    }

    @Override
    @Transactional
    public Comment createComment(Long userId, Long postId, CreateCommentRequest request) {
        var post = postMapper.selectById(postId);
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setId(idGenerator.nextId());
        comment.setContent(request.getContent());
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setStatus("PUBLISHED");

        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new ResourceNotFoundException("父评论不存在");
            }
            comment.setParentId(request.getParentId());
            comment.setRootId(parent.getRootId() != null ? parent.getRootId() : parent.getId());
            comment.setReplyToUserId(parent.getUserId());
        }

        commentMapper.insert(comment);
        postMapper.updateCommentCount(postId, 1);

        // Send notification
        User sender = userMapper.selectById(userId);
        String senderName = sender != null ? sender.getUsername() : "用户" + userId;
        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent != null && !parent.getUserId().equals(userId) && parent.getUserId() != BOT_USER_ID) {
                notificationService.createNotification(parent.getUserId(), "REPLY", userId,
                    senderName + " 回复了你的评论", truncateText(comment.getContent(), 100),
                    "POST", postId);
            }
        } else {
            if (!post.getUserId().equals(userId)) {
                notificationService.createNotification(post.getUserId(), "REPLY", userId,
                    senderName + " 回复了你的帖子", truncateText(comment.getContent(), 100),
                    "POST", postId);
            }
        }

        moderationProducer.sendModerationTask("COMMENT", comment.getId(), userId, comment.getContent());
        aiAgentService.handleMention(comment);
        syncCommentToEs(comment);

        return comment;
    }

    @Override
    public List<Comment> getCommentTree(Long postId) {
        List<Comment> allComments = commentMapper.selectByPostId(postId);
        Map<Long, List<Comment>> childrenMap = new HashMap<>();
        List<Comment> roots = new ArrayList<>();

        for (Comment c : allComments) {
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }

        // Attach children to each root recursively (setting them on the parent won't work without a children field)
        // For simplicity, we sort them and return flat — frontend builds tree
        return allComments;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该评论");
        }
        commentMapper.updateStatus(commentId, "REMOVED");
        postMapper.updateCommentCount(comment.getPostId(), -1);
        deleteCommentFromEs(commentId);
    }

    @Override
    public void updateStatus(Long commentId, String status) {
        commentMapper.updateStatus(commentId, status);
        if ("REMOVED".equals(status)) {
            deleteCommentFromEs(commentId);
        } else {
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) syncCommentToEs(comment);
        }
    }

    @Override
    public void incrementLikeCount(Long commentId, int delta) {
        commentMapper.updateLikeCount(commentId, delta);
    }

    private String truncateText(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }

    private void syncCommentToEs(Comment comment) {
        try {
            commentSearchRepo.save(CommentDocument.from(comment));
        } catch (Exception e) {
            log.error("ES sync failed for comment {}", comment.getId(), e);
        }
    }

    private void deleteCommentFromEs(Long commentId) {
        try {
            commentSearchRepo.deleteById(commentId);
        } catch (Exception e) {
            log.error("ES delete failed for comment {}", commentId, e);
        }
    }
}
