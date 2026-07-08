package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    @Insert("INSERT IGNORE INTO bookmarks (id, user_id, post_id, created_at) VALUES (#{id}, #{userId}, #{postId}, NOW())")
    int insert(Bookmark bookmark);

    @Delete("DELETE FROM bookmarks WHERE user_id=#{userId} AND post_id=#{postId}")
    int delete(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id=#{userId} AND post_id=#{postId}")
    int exists(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT p.* FROM posts p INNER JOIN bookmarks b ON p.id = b.post_id " +
            "WHERE b.user_id=#{userId} AND p.status='PUBLISHED' ORDER BY b.created_at DESC LIMIT #{offset}, #{size}")
    List<Long> selectBookmarkedPostIds(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id=#{userId}")
    long countByUserId(Long userId);
}
