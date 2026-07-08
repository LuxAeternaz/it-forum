package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.entity.Bookmark;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.mapper.BookmarkMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.service.BookmarkService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkMapper bookmarkMapper;
    private final PostMapper postMapper;
    private final SnowflakeIdGenerator idGenerator;

    public BookmarkServiceImpl(BookmarkMapper bookmarkMapper, PostMapper postMapper,
                               SnowflakeIdGenerator idGenerator) {
        this.bookmarkMapper = bookmarkMapper;
        this.postMapper = postMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public void bookmark(Long userId, Long postId) {
        if (bookmarkMapper.exists(userId, postId) > 0) return;
        Bookmark bookmark = new Bookmark();
        bookmark.setId(idGenerator.nextId());
        bookmark.setUserId(userId);
        bookmark.setPostId(postId);
        bookmarkMapper.insert(bookmark);
    }

    @Override
    public void unbookmark(Long userId, Long postId) {
        bookmarkMapper.delete(userId, postId);
    }

    @Override
    public boolean isBookmarked(Long userId, Long postId) {
        return bookmarkMapper.exists(userId, postId) > 0;
    }

    @Override
    public PageResult<Post> getBookmarks(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Long> postIds = bookmarkMapper.selectBookmarkedPostIds(userId, offset, size);
        List<Post> posts = new ArrayList<>();
        for (Long postId : postIds) {
            Post post = postMapper.selectById(postId);
            if (post != null) posts.add(post);
        }
        long total = bookmarkMapper.countByUserId(userId);
        return new PageResult<>(posts, total, page, size);
    }
}
