package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Notification;

public interface NotificationService {
    void createNotification(Long userId, String type, Long senderId, String title, String content, String targetType, Long targetId);
    PageResult<Notification> listNotifications(Long userId, String type, int page, int size);
    int getUnreadCount(Long userId);
    void markRead(Long userId, Long notificationId);
    void markAllRead(Long userId);
}
