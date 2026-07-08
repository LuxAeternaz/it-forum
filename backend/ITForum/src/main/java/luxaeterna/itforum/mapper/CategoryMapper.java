package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM categories ORDER BY sort_order ASC")
    List<Category> selectAll();

    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category selectById(Integer id);

    @Insert("INSERT INTO categories (name, description, icon, sort_order, created_at) VALUES (#{name}, #{description}, #{icon}, #{sortOrder}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE categories SET name=#{name}, description=#{description}, icon=#{icon}, sort_order=#{sortOrder} WHERE id=#{id}")
    int update(Category category);

    @Update("UPDATE categories SET post_count = (SELECT COUNT(*) FROM posts WHERE category_id=#{id} AND status='PUBLISHED') WHERE id=#{id}")
    int updatePostCount(Integer id);

    @Delete("DELETE FROM categories WHERE id=#{id}")
    int deleteById(Integer id);
}
