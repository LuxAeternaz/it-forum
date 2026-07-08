package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.entity.Category;
import luxaeterna.itforum.mapper.*;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final ModerationLogMapper moderationLogMapper;
    private final SearchService searchService;

    public AdminController(UserMapper userMapper, PostMapper postMapper,
                           CommentMapper commentMapper, CategoryMapper categoryMapper,
                           ModerationLogMapper moderationLogMapper, SearchService searchService) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.categoryMapper = categoryMapper;
        this.moderationLogMapper = moderationLogMapper;
        this.searchService = searchService;
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        long totalUsers = userMapper.count();
        long totalPosts = postMapper.countAll();
        long totalComments = commentMapper.countAll();
        String today = LocalDate.now().toString();
        long todayPosts = postMapper.countToday(today);
        long todayComments = commentMapper.countToday(today);
        long pendingModeration = moderationLogMapper.countPending();

        return Result.success(Map.of(
            "totalUsers", totalUsers,
            "totalPosts", totalPosts,
            "totalComments", totalComments,
            "todayPosts", todayPosts,
            "todayComments", todayComments,
            "pendingModeration", pendingModeration
        ));
    }

    // ==================== Users ====================

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        return Result.success(Map.of(
            "list", userMapper.selectList(offset, size),
            "total", userMapper.count()
        ));
    }

    @PutMapping("/users/{userId}/role")
    public Result<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        userMapper.updateRole(userId, body.get("role"));
        return Result.success();
    }

    @PutMapping("/users/{userId}/status")
    public Result<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> body) {
        userMapper.updateStatus(userId, body.get("status"));
        return Result.success();
    }

    // ==================== Posts ====================

    @GetMapping("/posts")
    public Result<?> listPosts(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String keyword) {
        int offset = (page - 1) * size;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return Result.success(Map.of(
            "list", postMapper.selectAll(kw, offset, size),
            "total", postMapper.countAllFiltered(kw)
        ));
    }

    @DeleteMapping("/posts/{postId}")
    public Result<?> deletePost(@PathVariable Long postId) {
        postMapper.updateStatus(postId, "REMOVED");
        return Result.success();
    }

    // ==================== Categories ====================

    @GetMapping("/categories")
    public Result<?> listCategories() {
        return Result.success(categoryMapper.selectAll());
    }

    @PostMapping("/categories")
    public Result<?> createCategory(@RequestBody Category category) {
        if (category.getSortOrder() == null) {
            List<Category> all = categoryMapper.selectAll();
            category.setSortOrder(all.isEmpty() ? 1 : all.get(all.size() - 1).getSortOrder() + 1);
        }
        categoryMapper.insert(category);
        return Result.success(category);
    }

    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.update(category);
        return Result.success();
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // ==================== Moderation ====================

    @GetMapping("/moderation/logs")
    public Result<?> getModerationLogs(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        return Result.success(Map.of(
            "list", moderationLogMapper.selectList(offset, size),
            "total", moderationLogMapper.count()
        ));
    }

    @PutMapping("/moderation/{logId}/review")
    public Result<?> reviewModeration(@CurrentUser Long userId, @PathVariable Long logId) {
        moderationLogMapper.updateReview(logId, userId);
        return Result.success();
    }

    @PutMapping("/moderation/{logId}/restore")
    public Result<?> restoreModeration(@CurrentUser Long userId, @PathVariable Long logId) {
        var log = moderationLogMapper.selectById(logId);
        if (log == null) {
            return Result.error(404, "审核日志不存在");
        }
        // Restore content status to PUBLISHED
        if ("POST".equals(log.getTargetType())) {
            postMapper.updateStatus(log.getTargetId(), "PUBLISHED");
        } else if ("COMMENT".equals(log.getTargetType())) {
            commentMapper.updateStatus(log.getTargetId(), "PUBLISHED");
        }
        moderationLogMapper.updateReview(logId, userId);
        return Result.success("已恢复并标记为已审核");
    }

    @PostMapping("/moderation/review-all-approved")
    public Result<?> reviewAllApproved(@CurrentUser Long userId) {
        int count = moderationLogMapper.batchReviewApproved(userId);
        return Result.success("已将 " + count + " 条通过审核的日志标记为已审核");
    }

    @GetMapping("/moderation/pending-count")
    public Result<?> getPendingCount() {
        return Result.success(Map.of(
            "pending", moderationLogMapper.countPending(),
            "approvedPending", moderationLogMapper.countApprovedPending()
        ));
    }
}
