package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Notification;
import luxaeterna.itforum.mapper.NotificationMapper;
import luxaeterna.itforum.service.NotificationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SnowflakeIdGenerator idGenerator;

    public NotificationServiceImpl(NotificationMapper notificationMapper, SnowflakeIdGenerator idGenerator) {
        this.notificationMapper = notificationMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public void createNotification(Long userId, String type, Long senderId, String title,
                                   String content, String targetType, Long targetId) {
        Notification notification = new Notification();
        notification.setId(idGenerator.nextId());
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSenderId(senderId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notificationMapper.insert(notification);
    }

    @Override
    public PageResult<Notification> listNotifications(Long userId, String type, int page, int size) {
        int offset = (page - 1) * size;
        List<Notification> list = notificationMapper.selectList(userId, type, offset, size);
        long total = notificationMapper.count(userId, type);
        return new PageResult<>(list, total, page, size);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        notificationMapper.markRead(notificationId, userId);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }
}
