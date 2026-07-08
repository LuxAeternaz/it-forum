package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.ModerationLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ModerationLogMapper {

    @Insert("INSERT INTO moderation_logs (id, target_type, target_id, user_id, content_snippet, result, reason, confidence, created_at) " +
            "VALUES (#{id}, #{targetType}, #{targetId}, #{userId}, #{contentSnippet}, #{result}, #{reason}, #{confidence}, NOW())")
    int insert(ModerationLog log);

    @Select("SELECT * FROM moderation_logs WHERE id = #{id}")
    ModerationLog selectById(Long id);

    @Select("SELECT * FROM moderation_logs ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<ModerationLog> selectList(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM moderation_logs")
    long count();

    @Update("UPDATE moderation_logs SET reviewed_by=#{reviewedBy}, reviewed_at=NOW() WHERE id=#{id}")
    int updateReview(@Param("id") Long id, @Param("reviewedBy") Long reviewedBy);

    @Select("SELECT COUNT(*) FROM moderation_logs WHERE reviewed_by IS NULL")
    long countPending();

    @Select("SELECT COUNT(*) FROM moderation_logs WHERE reviewed_by IS NULL AND result='APPROVED'")
    long countApprovedPending();

    @Update("UPDATE moderation_logs SET reviewed_by=#{reviewedBy}, reviewed_at=NOW() WHERE reviewed_by IS NULL AND result='APPROVED'")
    int batchReviewApproved(@Param("reviewedBy") Long reviewedBy);
}
