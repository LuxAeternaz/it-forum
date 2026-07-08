package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<?> getProfile(@CurrentUser Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public Result<?> updateProfile(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        return Result.success(userService.updateProfile(userId, body.get("username"), body.get("bio")));
    }

    @PutMapping("/me/password")
    public Result<?> changePassword(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/me/avatar")
    public Result<?> updateAvatar(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        return Result.success(userService.updateAvatar(userId, body.get("avatarUrl")));
    }

    @GetMapping("/{userId}")
    public Result<?> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @GetMapping("/{userId}/posts")
    public Result<PageResult<Post>> getUserPosts(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getUserPosts(userId, page, size));
    }

    @GetMapping("/{userId}/comments")
    public Result<PageResult<Comment>> getUserComments(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getUserComments(userId, page, size));
    }
}
