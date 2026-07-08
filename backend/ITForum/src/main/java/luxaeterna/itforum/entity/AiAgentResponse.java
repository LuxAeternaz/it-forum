package luxaeterna.itforum.entity;

import java.time.LocalDateTime;

public class AiAgentResponse {
    private Long id;
    private Long triggerUserId;
    private Long commentId;
    private Long postId;
    private Long parentCommentId;
    private Long agentId;
    private String prompt;
    private String response;
    private String modelUsed;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTriggerUserId() { return triggerUserId; }
    public void setTriggerUserId(Long triggerUserId) { this.triggerUserId = triggerUserId; }
    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
