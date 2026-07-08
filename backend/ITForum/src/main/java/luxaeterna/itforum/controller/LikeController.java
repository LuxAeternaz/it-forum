package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.LikeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public Result<?> like(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        likeService.like(userId, targetType, targetId);
        return Result.success();
    }

    @DeleteMapping
    public Result<?> unlike(@CurrentUser Long userId, @RequestBody Map<String, Object> body) {
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        likeService.unlike(userId, targetType, targetId);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<?> getLikeStatus(@CurrentUser Long userId,
                                    @RequestParam String targetType,
                                    @RequestParam List<Long> targetIds) {
        return Result.success(likeService.getLikedTargetIds(userId, targetType, targetIds));
    }
}
