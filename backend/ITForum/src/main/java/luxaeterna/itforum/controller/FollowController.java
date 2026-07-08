package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.FollowService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    public Result<?> follow(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        Long followeeId = Long.valueOf(body.get("followeeId").toString());
        String followType = (String) body.get("followType");
        followService.follow(userId, followeeId, followType);
        return Result.success();
    }

    @DeleteMapping
    public Result<?> unfollow(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        Long followeeId = Long.valueOf(body.get("followeeId").toString());
        String followType = (String) body.get("followType");
        followService.unfollow(userId, followeeId, followType);
        return Result.success();
    }

    @GetMapping("/followers")
    public Result<?> getFollowers(@RequestParam Long userId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowers(userId, page, size));
    }

    @GetMapping("/following")
    public Result<?> getFollowing(@RequestParam Long userId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.getFollowing(userId, page, size));
    }

    @GetMapping("/status")
    public Result<?> checkFollow(@CurrentUser Long userId,
                                  @RequestParam Long followeeId,
                                  @RequestParam String followType) {
        return Result.success(followService.isFollowing(userId, followeeId, followType));
    }
}
