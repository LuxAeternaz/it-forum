package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Like;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.entity.User;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.LikeMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.mapper.UserMapper;
import luxaeterna.itforum.service.LikeService;
import luxaeterna.itforum.service.NotificationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final NotificationService notificationService;

    public LikeServiceImpl(LikeMapper likeMapper, PostMapper postMapper,
                           CommentMapper commentMapper, UserMapper userMapper,
                           SnowflakeIdGenerator idGenerator,
                           NotificationService notificationService) {
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void like(Long userId, String targetType, Long targetId) {
        if (likeMapper.exists(userId, targetType, targetId) > 0) {
            return;
        }
        Like like = new Like();
        like.setId(idGenerator.nextId());
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeMapper.insert(like);

        if ("POST".equals(targetType)) {
            postMapper.updateLikeCount(targetId, 1);
            Post post = postMapper.selectById(targetId);
            if (post != null && !post.getUserId().equals(userId)) {
                User sender = userMapper.selectById(userId);
                String senderName = sender != null ? sender.getUsername() : "用户" + userId;
                notificationService.createNotification(post.getUserId(), "LIKE", userId,
                    senderName + " 赞了你的帖子", post.getTitle(),
                    "POST", targetId);
            }
        } else if ("COMMENT".equals(targetType)) {
            commentMapper.updateLikeCount(targetId, 1);
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null && !comment.getUserId().equals(userId)) {
                User sender = userMapper.selectById(userId);
                String senderName = sender != null ? sender.getUsername() : "用户" + userId;
                notificationService.createNotification(comment.getUserId(), "LIKE", userId,
                    senderName + " 赞了你的评论", truncateText(comment.getContent(), 100),
                    "POST", comment.getPostId());
            }
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, String targetType, Long targetId) {
        if (likeMapper.exists(userId, targetType, targetId) == 0) {
            return;
        }
        likeMapper.delete(userId, targetType, targetId);

        if ("POST".equals(targetType)) {
            postMapper.updateLikeCount(targetId, -1);
        } else if ("COMMENT".equals(targetType)) {
            commentMapper.updateLikeCount(targetId, -1);
        }
    }

    @Override
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        return likeMapper.exists(userId, targetType, targetId) > 0;
    }

    @Override
    public List<Long> getLikedTargetIds(Long userId, String targetType, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return Collections.emptyList();
        return likeMapper.selectLikedTargetIds(userId, targetType, targetIds);
    }

    private String truncateText(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }
}
