package luxaeterna.itforum.dto.response;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.es.PostDocument;

public class SearchResult {

    private PageResult<PostDocument> posts;
    private PageResult<CommentSearchHit> comments;

    public PageResult<PostDocument> getPosts() { return posts; }
    public void setPosts(PageResult<PostDocument> posts) { this.posts = posts; }
    public PageResult<CommentSearchHit> getComments() { return comments; }
    public void setComments(PageResult<CommentSearchHit> comments) { this.comments = comments; }
}
