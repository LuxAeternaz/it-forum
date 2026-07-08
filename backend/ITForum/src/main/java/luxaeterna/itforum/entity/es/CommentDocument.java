package luxaeterna.itforum.entity.es;

import luxaeterna.itforum.entity.Comment;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "itforum_comments")
public class CommentDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long postId;

    @Field(type = FieldType.Long)
    private Long parentId;

    @Field(type = FieldType.Long)
    private Long rootId;

    @Field(type = FieldType.Long)
    private Long replyToUserId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword, index = false)
    private String avatarUrl;

    public static CommentDocument from(Comment comment) {
        CommentDocument doc = new CommentDocument();
        doc.id = comment.getId();
        doc.content = comment.getContent();
        doc.userId = comment.getUserId();
        doc.postId = comment.getPostId();
        doc.parentId = comment.getParentId();
        doc.rootId = comment.getRootId();
        doc.replyToUserId = comment.getReplyToUserId();
        doc.status = comment.getStatus();
        doc.likeCount = comment.getLikeCount();
        doc.createdAt = comment.getCreatedAt();
        doc.username = comment.getUsername();
        doc.avatarUrl = comment.getAvatarUrl();
        return doc;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
