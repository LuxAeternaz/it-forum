package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Like;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeMapper {

    @Insert("INSERT IGNORE INTO likes (id, user_id, target_type, target_id, created_at) " +
            "VALUES (#{id}, #{userId}, #{targetType}, #{targetId}, NOW())")
    int insert(Like like);

    @Delete("DELETE FROM likes WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) FROM likes WHERE user_id=#{userId} AND target_type=#{targetType} AND target_id=#{targetId}")
    int exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Select("<script>SELECT target_id FROM likes WHERE user_id=#{userId} AND target_type=#{targetType} " +
            "AND target_id IN <foreach collection='targetIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> selectLikedTargetIds(@Param("userId") Long userId, @Param("targetType") String targetType,
                                    @Param("targetIds") List<Long> targetIds);

    @Select("SELECT COUNT(*) FROM likes WHERE " +
            "(target_type = 'POST' AND target_id IN (SELECT id FROM posts WHERE user_id = #{userId})) " +
            "OR (target_type = 'COMMENT' AND target_id IN (SELECT id FROM comments WHERE user_id = #{userId}))")
    long countLikesReceived(Long userId);
}
