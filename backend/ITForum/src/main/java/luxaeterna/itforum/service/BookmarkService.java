package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Post;

public interface BookmarkService {
    void bookmark(Long userId, Long postId);
    void unbookmark(Long userId, Long postId);
    boolean isBookmarked(Long userId, Long postId);
    PageResult<Post> getBookmarks(Long userId, int page, int size);
}
