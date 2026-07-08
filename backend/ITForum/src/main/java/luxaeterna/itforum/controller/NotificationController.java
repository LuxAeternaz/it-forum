package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<?> list(@CurrentUser Long userId,
                           @RequestParam(required = false) String type,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        return Result.success(notificationService.listNotifications(userId, type, page, size));
    }

    @GetMapping("/unread-count")
    public Result<?> unreadCount(@CurrentUser Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<?> markRead(@CurrentUser Long userId, @PathVariable Long id) {
        notificationService.markRead(userId, id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<?> markAllRead(@CurrentUser Long userId) {
        notificationService.markAllRead(userId);
        return Result.success();
    }
}
