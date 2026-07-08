package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.request.CreatePostRequest;
import luxaeterna.itforum.entity.Post;

public interface PostService {
    Post createPost(Long userId, CreatePostRequest request);
    Post getPost(Long id);
    Post updatePost(Long userId, Long postId, CreatePostRequest request);
    void deletePost(Long userId, Long postId);
    PageResult<Post> listPosts(Integer categoryId, String sort, int page, int size);
    void incrementViewCount(Long postId);
    void updateStatus(Long postId, String status);
    void togglePin(Long postId);
    void toggleEssence(Long postId);
}
