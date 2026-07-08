package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Draft;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DraftMapper {

    @Insert("INSERT INTO drafts (id, user_id, post_id, title, content, category_id, version, updated_at, created_at) " +
            "VALUES (#{id}, #{userId}, #{postId}, #{title}, #{content}, #{categoryId}, 1, NOW(), NOW())")
    int insert(Draft draft);

    @Update("UPDATE drafts SET title=#{title}, content=#{content}, category_id=#{categoryId}, " +
            "version=version+1, updated_at=NOW() WHERE id=#{id}")
    int update(Draft draft);

    @Select("SELECT * FROM drafts WHERE user_id=#{userId} AND (post_id=#{postId} OR (post_id IS NULL AND #{postId} IS NULL))")
    Draft selectByUserAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Delete("DELETE FROM drafts WHERE user_id=#{userId} AND (post_id=#{postId} OR (post_id IS NULL AND #{postId} IS NULL))")
    int deleteByUserAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
