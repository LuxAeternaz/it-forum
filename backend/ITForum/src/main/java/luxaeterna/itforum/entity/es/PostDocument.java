package luxaeterna.itforum.entity.es;

import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.util.MarkdownUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "itforum_posts")
public class PostDocument {

    @Id
    private Long id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Integer)
    private Integer categoryId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private Integer isPinned;

    @Field(type = FieldType.Integer)
    private Integer isEssence;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime lastRepliedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword, index = false)
    private String avatarUrl;

    public static PostDocument from(Post post) {
        PostDocument doc = new PostDocument();
        doc.id = post.getId();
        doc.title = post.getTitle();
        doc.content = MarkdownUtil.stripMarkdown(post.getContent());
        doc.userId = post.getUserId();
        doc.categoryId = post.getCategoryId();
        doc.status = post.getStatus();
        doc.isPinned = post.getIsPinned();
        doc.isEssence = post.getIsEssence();
        doc.viewCount = post.getViewCount();
        doc.commentCount = post.getCommentCount();
        doc.likeCount = post.getLikeCount();
        doc.lastRepliedAt = post.getLastRepliedAt();
        doc.createdAt = post.getCreatedAt();
        doc.username = post.getUsername();
        doc.avatarUrl = post.getAvatarUrl();
        return doc;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
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
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
