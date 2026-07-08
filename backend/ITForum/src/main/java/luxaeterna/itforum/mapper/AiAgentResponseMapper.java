package luxaeterna.itforum.mapper;

import luxaeterna.itforum.entity.AiAgentResponse;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiAgentResponseMapper {

    @Insert("INSERT INTO ai_agent_responses (id, trigger_user_id, comment_id, post_id, parent_comment_id, agent_id, prompt, response, model_used, tokens_used, created_at) " +
            "VALUES (#{id}, #{triggerUserId}, #{commentId}, #{postId}, #{parentCommentId}, #{agentId}, #{prompt}, #{response}, #{modelUsed}, #{tokensUsed}, NOW())")
    int insert(AiAgentResponse response);

    @Select("SELECT * FROM ai_agent_responses WHERE id = #{id}")
    AiAgentResponse selectById(Long id);

    @Select("SELECT * FROM ai_agent_responses WHERE parent_comment_id = #{commentId}")
    AiAgentResponse selectByParentCommentId(Long commentId);
}
