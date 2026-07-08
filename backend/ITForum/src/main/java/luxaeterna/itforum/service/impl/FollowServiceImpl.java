package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Follow;
import luxaeterna.itforum.entity.User;
import luxaeterna.itforum.mapper.FollowMapper;
import luxaeterna.itforum.mapper.UserMapper;
import luxaeterna.itforum.service.FollowService;
import luxaeterna.itforum.service.NotificationService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final NotificationService notificationService;

    public FollowServiceImpl(FollowMapper followMapper, UserMapper userMapper,
                             SnowflakeIdGenerator idGenerator,
                             NotificationService notificationService) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void follow(Long followerId, Long followeeId, String followType) {
        if (followMapper.exists(followerId, followeeId, followType) > 0) return;
        Follow follow = new Follow();
        follow.setId(idGenerator.nextId());
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setFollowType(followType);
        followMapper.insert(follow);

        // Notify the followed user
        User follower = userMapper.selectById(followerId);
        String followerName = follower != null ? follower.getUsername() : "用户" + followerId;
        notificationService.createNotification(followeeId, "FOLLOW", followerId,
            followerName + " 关注了你", null, "USER", followerId);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId, String followType) {
        followMapper.delete(followerId, followeeId, followType);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId, String followType) {
        return followMapper.exists(followerId, followeeId, followType) > 0;
    }

    @Override
    public PageResult<Follow> getFollowers(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Follow> list = followMapper.selectFollowers(userId, offset, size);
        long total = followMapper.countFollowers(userId);
        return new PageResult<>(list, total, page, size);
    }

    @Override
    public PageResult<Follow> getFollowing(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Follow> list = followMapper.selectFollowing(userId, offset, size);
        long total = followMapper.countFollowing(userId);
        return new PageResult<>(list, total, page, size);
    }
}
