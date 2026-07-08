package luxaeterna.itforum.service;

import java.util.List;

public interface LikeService {
    void like(Long userId, String targetType, Long targetId);
    void unlike(Long userId, String targetType, Long targetId);
    boolean isLiked(Long userId, String targetType, Long targetId);
    List<Long> getLikedTargetIds(Long userId, String targetType, List<Long> targetIds);
}
