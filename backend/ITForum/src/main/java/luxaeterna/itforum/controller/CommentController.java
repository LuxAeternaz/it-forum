package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.CreateCommentRequest;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<?> getCommentTree(@PathVariable Long postId) {
        return Result.success(commentService.getCommentTree(postId));
    }

    @PostMapping
    public Result<?> createComment(@CurrentUser Long userId, @PathVariable Long postId,
                                    @Valid @RequestBody CreateCommentRequest request) {
        return Result.success(commentService.createComment(userId, postId, request));
    }

    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(@CurrentUser Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return Result.success();
    }
}
