package luxaeterna.itforum.entity;

import java.time.LocalDateTime;

public class Post {
    private Long id;
    private String title;
    private String content;
    private String contentHtml;
    private Long userId;
    private Integer categoryId;
    private String status;
    private Integer isPinned;
    private Integer isEssence;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private LocalDateTime lastRepliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String username;
    private String avatarUrl;
    private String lastReplyUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIsPinned() { return isPinned; }
    public void setIsPinned(Integer isPinned) { this.isPinned = isPinned; }
    public Integer getIsEssence() { return isEssence; }
    public void setIsEssence(Integer isEssence) { this.isEssence = isEssence; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getLastRepliedAt() { return lastRepliedAt; }
    public void setLastRepliedAt(LocalDateTime lastRepliedAt) { this.lastRepliedAt = lastRepliedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getLastReplyUsername() { return lastReplyUsername; }
    public void setLastReplyUsername(String lastReplyUsername) { this.lastReplyUsername = lastReplyUsername; }
}
