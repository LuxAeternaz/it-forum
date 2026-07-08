package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Follow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {

    @Insert("INSERT IGNORE INTO follows (id, follower_id, followee_id, follow_type, created_at) " +
            "VALUES (#{id}, #{followerId}, #{followeeId}, #{followType}, NOW())")
    int insert(Follow follow);

    @Delete("DELETE FROM follows WHERE follower_id=#{followerId} AND followee_id=#{followeeId} AND follow_type=#{followType}")
    int delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId, @Param("followType") String followType);

    @Select("SELECT COUNT(*) FROM follows WHERE follower_id=#{followerId} AND followee_id=#{followeeId} AND follow_type=#{followType}")
    int exists(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId, @Param("followType") String followType);

    @Select("SELECT * FROM follows WHERE followee_id=#{userId} AND follow_type='USER' ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Follow> selectFollowers(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM follows WHERE follower_id=#{userId} AND follow_type='USER' ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Follow> selectFollowing(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM follows WHERE followee_id=#{userId} AND follow_type='USER'")
    long countFollowers(Long userId);

    @Select("SELECT COUNT(*) FROM follows WHERE follower_id=#{userId} AND follow_type='USER'")
    long countFollowing(Long userId);

    @Select("SELECT followee_id FROM follows WHERE follower_id=#{userId} AND follow_type='CATEGORY'")
    List<Long> selectFollowedCategoryIds(Long userId);

    @Select("SELECT u.id, u.username, u.avatar_url AS avatarUrl FROM users u " +
            "WHERE u.id IN (" +
            "  SELECT f1.followee_id FROM follows f1 " +
            "  INNER JOIN follows f2 ON f1.followee_id = f2.follower_id " +
            "  WHERE f1.follower_id = #{userId} AND f2.followee_id = #{userId} " +
            "  AND f1.follow_type = 'USER' AND f2.follow_type = 'USER'" +
            ") ORDER BY u.username")
    List<java.util.Map<String, Object>> selectMutualFollows(Long userId);
}
