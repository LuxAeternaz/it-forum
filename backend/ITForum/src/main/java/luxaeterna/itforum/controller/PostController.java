package luxaeterna.itforum.controller;

import jakarta.validation.Valid;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.request.CreatePostRequest;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.PostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Result<?> listPosts(@RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false, defaultValue = "latest") String sort,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.listPosts(categoryId, sort, page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getPost(@PathVariable Long id) {
        postService.incrementViewCount(id);
        return Result.success(postService.getPost(id));
    }

    @PostMapping
    public Result<?> createPost(@CurrentUser Long userId, @Valid @RequestBody CreatePostRequest request) {
        return Result.success(postService.createPost(userId, request));
    }

    @PutMapping("/{id}")
    public Result<?> updatePost(@CurrentUser Long userId, @PathVariable Long id,
                                 @Valid @RequestBody CreatePostRequest request) {
        return Result.success(postService.updatePost(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> deletePost(@CurrentUser Long userId, @PathVariable Long id) {
        postService.deletePost(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/pin")
    public Result<?> togglePin(@PathVariable Long id) {
        postService.togglePin(id);
        return Result.success();
    }

    @PutMapping("/{id}/essence")
    public Result<?> toggleEssence(@PathVariable Long id) {
        postService.toggleEssence(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody String status) {
        postService.updateStatus(id, status);
        return Result.success();
    }
}
