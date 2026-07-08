package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Agent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AgentMapper {

    @Insert("INSERT INTO agents (id, name, persona, description, avatar_url, creator_user_id, status, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{persona}, #{description}, #{avatarUrl}, #{creatorUserId}, #{status}, NOW(), NOW())")
    int insert(Agent agent);

    @Select("SELECT a.*, u.username AS creator_username FROM agents a LEFT JOIN users u ON a.creator_user_id = u.id WHERE a.id = #{id}")
    Agent selectById(Long id);

    @Select("SELECT a.*, u.username AS creator_username FROM agents a LEFT JOIN users u ON a.creator_user_id = u.id WHERE a.name = #{name}")
    Agent selectByName(String name);

    @Select("SELECT a.*, u.username AS creator_username FROM agents a LEFT JOIN users u ON a.creator_user_id = u.id WHERE a.status = 'ACTIVE' ORDER BY a.created_at DESC")
    List<Agent> selectAllActive();

    @Select("SELECT a.*, u.username AS creator_username FROM agents a LEFT JOIN users u ON a.creator_user_id = u.id WHERE a.creator_user_id = #{userId} ORDER BY a.created_at DESC")
    List<Agent> selectByCreatorUserId(Long userId);

    @Update("UPDATE agents SET name = #{name}, persona = #{persona}, description = #{description}, avatar_url = #{avatarUrl}, updated_at = NOW() WHERE id = #{id}")
    int update(Agent agent);

    @Update("UPDATE agents SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM agents WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM agents WHERE name = #{name}")
    int countByName(String name);

    @Select("SELECT a.*, u.username AS creator_username FROM agents a LEFT JOIN users u ON a.creator_user_id = u.id " +
            "WHERE a.status = 'ACTIVE' AND a.name LIKE CONCAT('%', #{keyword}, '%') ORDER BY a.name LIMIT #{limit}")
    List<Agent> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);
}
