package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Follow;

public interface FollowService {
    void follow(Long followerId, Long followeeId, String followType);
    void unfollow(Long followerId, Long followeeId, String followType);
    boolean isFollowing(Long followerId, Long followeeId, String followType);
    PageResult<Follow> getFollowers(Long userId, int page, int size);
    PageResult<Follow> getFollowing(Long userId, int page, int size);
}
