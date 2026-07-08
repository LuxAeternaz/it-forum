package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comments (id, content, user_id, post_id, parent_id, root_id, reply_to_user_id, " +
            "status, like_count, created_at) " +
            "VALUES (#{id}, #{content}, #{userId}, #{postId}, #{parentId}, #{rootId}, #{replyToUserId}, " +
            "#{status}, 0, NOW())")
    int insert(Comment comment);

    @Select("SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.id = #{id}")
    Comment selectById(Long id);

    @Select("SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.post_id=#{postId} AND c.status='PUBLISHED' ORDER BY c.root_id ASC, c.created_at ASC")
    List<Comment> selectByPostId(Long postId);

    @Select("SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.user_id=#{userId} AND c.status='PUBLISHED' ORDER BY c.created_at DESC LIMIT #{offset}, #{size}")
    List<Comment> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM comments WHERE user_id=#{userId} AND status='PUBLISHED'")
    long countByUserId(Long userId);

    @Update("UPDATE comments SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE comments SET like_count = like_count + #{delta} WHERE id=#{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Select("SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.id=#{id}")
    List<Comment> selectChildComments(Long id);

    @Select("SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id ORDER BY c.id LIMIT #{offset}, #{limit}")
    List<Comment> selectPublishedBatch(@Param("offset") long offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT c.*, u.username, u.avatar_url FROM comments c LEFT JOIN users u ON c.user_id = u.id WHERE c.status='PUBLISHED' " +
            "<if test='keyword != null and keyword != \"\"'>AND c.content LIKE CONCAT('%',#{keyword},'%')</if> " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{size}</script>")
    List<Comment> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM comments WHERE status='PUBLISHED' " +
            "<if test='keyword != null and keyword != \"\"'>AND content LIKE CONCAT('%',#{keyword},'%')</if></script>")
    long countSearch(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM comments")
    long countAll();

    @Select("SELECT COUNT(*) FROM comments WHERE DATE(created_at) = #{today}")
    long countToday(@Param("today") String today);
}
