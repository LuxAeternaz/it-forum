package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notifications (id, user_id, type, sender_id, title, content, target_type, target_id, is_read, created_at) " +
            "VALUES (#{id}, #{userId}, #{type}, #{senderId}, #{title}, #{content}, #{targetType}, #{targetId}, 0, NOW())")
    int insert(Notification notification);

    @Select("<script>SELECT * FROM notifications WHERE user_id=#{userId} " +
            "<if test='type != null'>AND type=#{type}</if> " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}</script>")
    List<Notification> selectList(@Param("userId") Long userId, @Param("type") String type,
                                  @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM notifications WHERE user_id=#{userId} " +
            "<if test='type != null'>AND type=#{type}</if></script>")
    long count(@Param("userId") Long userId, @Param("type") String type);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id=#{userId} AND is_read=0")
    int countUnread(Long userId);

    @Update("UPDATE notifications SET is_read=1 WHERE id=#{id} AND user_id=#{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notifications SET is_read=1 WHERE user_id=#{userId}")
    int markAllRead(Long userId);
}
