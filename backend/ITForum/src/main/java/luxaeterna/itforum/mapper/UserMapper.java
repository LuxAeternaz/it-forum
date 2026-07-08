package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (id, username, email, password_hash, avatar_url, bio, role, status, created_at, updated_at) " +
            "VALUES (#{id}, #{username}, #{email}, #{passwordHash}, #{avatarUrl}, #{bio}, #{role}, #{status}, NOW(), NOW())")
    int insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(String email);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM users ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<User> selectList(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM users")
    long count();

    @Update("UPDATE users SET username=#{username}, bio=#{bio}, avatar_url=#{avatarUrl}, updated_at=NOW() WHERE id=#{id}")
    int updateProfile(User user);

    @Update("UPDATE users SET password_hash=#{passwordHash}, updated_at=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Update("UPDATE users SET avatar_url=#{avatarUrl}, updated_at=NOW() WHERE id=#{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE users SET role=#{role}, updated_at=NOW() WHERE id=#{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);

    @Update("UPDATE users SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE users SET last_login_at=NOW() WHERE id=#{id}")
    int updateLoginTime(Long id);

    @Select("SELECT COUNT(*) FROM follows WHERE followee_id=#{userId} AND follow_type='USER'")
    int countFollowers(Long userId);

    @Select("SELECT COUNT(*) FROM follows WHERE follower_id=#{userId} AND follow_type='USER'")
    int countFollowing(Long userId);
}
