package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.BookmarkService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{postId}")
    public Result<?> bookmark(@CurrentUser Long userId, @PathVariable Long postId) {
        bookmarkService.bookmark(userId, postId);
        return Result.success();
    }

    @DeleteMapping("/{postId}")
    public Result<?> unbookmark(@CurrentUser Long userId, @PathVariable Long postId) {
        bookmarkService.unbookmark(userId, postId);
        return Result.success();
    }

    @GetMapping
    public Result<?> getBookmarks(@CurrentUser Long userId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return Result.success(bookmarkService.getBookmarks(userId, page, size));
    }

    @GetMapping("/{postId}/status")
    public Result<?> isBookmarked(@CurrentUser Long userId, @PathVariable Long postId) {
        return Result.success(bookmarkService.isBookmarked(userId, postId));
    }
}
