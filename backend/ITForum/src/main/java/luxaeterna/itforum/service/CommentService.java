package luxaeterna.itforum.service;

import luxaeterna.itforum.dto.request.CreateCommentRequest;
import luxaeterna.itforum.entity.Comment;
import java.util.List;

public interface CommentService {
    Comment createComment(Long userId, Long postId, CreateCommentRequest request);
    List<Comment> getCommentTree(Long postId);
    void deleteComment(Long userId, Long commentId);
    void updateStatus(Long commentId, String status);
    void incrementLikeCount(Long commentId, int delta);
}
