package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {

    @Insert("INSERT INTO posts (id, title, content, content_html, user_id, category_id, status, is_pinned, is_essence, " +
            "view_count, comment_count, like_count, created_at, updated_at) " +
            "VALUES (#{id}, #{title}, #{content}, #{contentHtml}, #{userId}, #{categoryId}, #{status}, " +
            "#{isPinned}, #{isEssence}, 0, 0, 0, NOW(), NOW())")
    int insert(Post post);

    @Select("SELECT p.*, u.username, u.avatar_url FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.id = #{id}")
    Post selectById(Long id);

    @Update("UPDATE posts SET title=#{title}, content=#{content}, content_html=#{contentHtml}, " +
            "category_id=#{categoryId}, updated_at=NOW() WHERE id=#{id}")
    int update(Post post);

    @Update("UPDATE posts SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM posts WHERE id=#{id}")
    int deleteById(Long id);

    @Select("<script>" +
            "SELECT p.*, u.username, u.avatar_url, " +
            "(SELECT u2.username FROM comments c2 LEFT JOIN users u2 ON c2.user_id = u2.id WHERE c2.post_id = p.id AND c2.status = 'PUBLISHED' ORDER BY c2.created_at DESC LIMIT 1) AS last_reply_username " +
            "FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.status='PUBLISHED' " +
            "<if test='categoryId != null'>AND p.category_id=#{categoryId}</if> " +
            "<if test='sort == \"hot\"'>ORDER BY (p.like_count * 2 + p.comment_count * 3 + p.view_count) DESC, p.created_at DESC</if>" +
            "<if test='sort != \"hot\"'>ORDER BY p.created_at DESC</if>" +
            "LIMIT #{offset}, #{size}</script>")
    List<Post> selectList(@Param("categoryId") Integer categoryId, @Param("sort") String sort,
                          @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM posts WHERE status='PUBLISHED' " +
            "<if test='categoryId != null'>AND category_id=#{categoryId}</if></script>")
    long count(@Param("categoryId") Integer categoryId);

    @Select("SELECT p.*, u.username, u.avatar_url, " +
            "(SELECT u2.username FROM comments c2 LEFT JOIN users u2 ON c2.user_id = u2.id WHERE c2.post_id = p.id AND c2.status = 'PUBLISHED' ORDER BY c2.created_at DESC LIMIT 1) AS last_reply_username " +
            "FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.user_id=#{userId} AND p.status='PUBLISHED' ORDER BY p.created_at DESC LIMIT #{offset}, #{size}")
    List<Post> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM posts WHERE user_id=#{userId} AND status='PUBLISHED'")
    long countByUserId(Long userId);

    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id=#{id}")
    int incrementViewCount(Long id);

    @Update("UPDATE posts SET view_count = view_count + #{count} WHERE id=#{id}")
    int addViewCount(@Param("id") Long id, @Param("count") int count);

    @Update("UPDATE posts SET comment_count = comment_count + #{delta}, last_replied_at=NOW() WHERE id=#{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE posts SET like_count = like_count + #{delta} WHERE id=#{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE posts SET is_pinned=#{pinned}, updated_at=NOW() WHERE id=#{id}")
    int updatePinned(@Param("id") Long id, @Param("pinned") Integer pinned);

    @Update("UPDATE posts SET is_essence=#{essence}, updated_at=NOW() WHERE id=#{id}")
    int updateEssence(@Param("id") Long id, @Param("essence") Integer essence);

    @Select("<script>" +
            "SELECT p.*, u.username, u.avatar_url, " +
            "(SELECT u2.username FROM comments c2 LEFT JOIN users u2 ON c2.user_id = u2.id WHERE c2.post_id = p.id AND c2.status = 'PUBLISHED' ORDER BY c2.created_at DESC LIMIT 1) AS last_reply_username " +
            "FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.status='PUBLISHED' " +
            "<if test='keyword != null and keyword != \"\"'>AND (p.title LIKE CONCAT('%',#{keyword},'%') OR p.content LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "<if test='categoryId != null'>AND p.category_id=#{categoryId}</if> " +
            "ORDER BY p.created_at DESC LIMIT #{offset}, #{size}</script>")
    List<Post> search(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId,
                      @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM posts WHERE status='PUBLISHED' " +
            "<if test='keyword != null and keyword != \"\"'>AND (title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "<if test='categoryId != null'>AND category_id=#{categoryId}</if></script>")
    long countSearch(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId);

    @Select("SELECT p.*, u.username, u.avatar_url FROM posts p LEFT JOIN users u ON p.user_id = u.id ORDER BY p.id LIMIT #{offset}, #{limit}")
    List<Post> selectPublishedBatch(@Param("offset") long offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM posts")
    long countAll();

    @Select("SELECT COUNT(*) FROM posts WHERE DATE(created_at) = #{today}")
    long countToday(@Param("today") String today);

    @Select("<script>" +
            "SELECT p.*, u.username, u.avatar_url, " +
            "(SELECT u2.username FROM comments c2 LEFT JOIN users u2 ON c2.user_id = u2.id WHERE c2.post_id = p.id AND c2.status = 'PUBLISHED' ORDER BY c2.created_at DESC LIMIT 1) AS last_reply_username " +
            "FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>AND (p.title LIKE CONCAT('%',#{keyword},'%') OR p.content LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "ORDER BY p.created_at DESC LIMIT #{offset}, #{size}</script>")
    List<Post> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM posts WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>AND (title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long countAllFiltered(@Param("keyword") String keyword);
}
