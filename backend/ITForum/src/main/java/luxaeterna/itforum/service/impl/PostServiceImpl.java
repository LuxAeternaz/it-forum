package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.request.CreatePostRequest;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.entity.es.PostDocument;
import luxaeterna.itforum.exception.BusinessException;
import luxaeterna.itforum.exception.ResourceNotFoundException;
import luxaeterna.itforum.mapper.CategoryMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.mq.producer.ModerationProducer;
import luxaeterna.itforum.repository.es.PostSearchRepository;
import luxaeterna.itforum.service.PostService;
import luxaeterna.itforum.util.MarkdownUtil;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ModerationProducer moderationProducer;
    private final PostSearchRepository postSearchRepo;

    public PostServiceImpl(PostMapper postMapper, CategoryMapper categoryMapper,
                           SnowflakeIdGenerator idGenerator,
                           ModerationProducer moderationProducer,
                           PostSearchRepository postSearchRepo) {
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.idGenerator = idGenerator;
        this.moderationProducer = moderationProducer;
        this.postSearchRepo = postSearchRepo;
    }

    @Override
    @Transactional
    public Post createPost(Long userId, CreatePostRequest request) {
        Post post = new Post();
        post.setId(idGenerator.nextId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setContentHtml(MarkdownUtil.toHtml(request.getContent()));
        post.setUserId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setStatus("PUBLISHED");
        post.setIsPinned(0);
        post.setIsEssence(0);
        postMapper.insert(post);
        categoryMapper.updatePostCount(post.getCategoryId());
        moderationProducer.sendModerationTask("POST", post.getId(), userId, post.getTitle() + " " + post.getContent());
        syncPostToEs(post);
        return post;
    }

    @Override
    public Post getPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || "REMOVED".equals(post.getStatus())) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        return post;
    }

    @Override
    @Transactional
    public Post updatePost(Long userId, Long postId, CreatePostRequest request) {
        Post post = getPost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改该帖子");
        }
        Integer oldCategoryId = post.getCategoryId();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setContentHtml(MarkdownUtil.toHtml(request.getContent()));
        post.setCategoryId(request.getCategoryId());
        postMapper.update(post);
        categoryMapper.updatePostCount(post.getCategoryId());
        if (oldCategoryId != null && !oldCategoryId.equals(post.getCategoryId())) {
            categoryMapper.updatePostCount(oldCategoryId);
        }
        syncPostToEs(post);
        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = getPost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除该帖子");
        }
        postMapper.updateStatus(postId, "REMOVED");
        categoryMapper.updatePostCount(post.getCategoryId());
        deletePostFromEs(postId);
    }

    @Override
    public PageResult<Post> listPosts(Integer categoryId, String sort, int page, int size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.selectList(categoryId, sort, offset, size);
        long total = postMapper.count(categoryId);
        return new PageResult<>(posts, total, page, size);
    }

    @Override
    public void incrementViewCount(Long postId) {
        postMapper.incrementViewCount(postId);
    }

    @Override
    public void updateStatus(Long postId, String status) {
        postMapper.updateStatus(postId, status);
        if ("REMOVED".equals(status)) {
            deletePostFromEs(postId);
        } else {
            Post post = postMapper.selectById(postId);
            if (post != null) syncPostToEs(post);
        }
    }

    @Override
    public void togglePin(Long postId) {
        Post post = getPost(postId);
        postMapper.updatePinned(postId, post.getIsPinned() == 1 ? 0 : 1);
    }

    @Override
    public void toggleEssence(Long postId) {
        Post post = getPost(postId);
        postMapper.updateEssence(postId, post.getIsEssence() == 1 ? 0 : 1);
        syncPostToEs(post);
    }

    private void syncPostToEs(Post post) {
        try {
            postSearchRepo.save(PostDocument.from(post));
        } catch (Exception e) {
            log.error("ES sync failed for post {}", post.getId(), e);
        }
    }

    private void deletePostFromEs(Long postId) {
        try {
            postSearchRepo.deleteById(postId);
        } catch (Exception e) {
            log.error("ES delete failed for post {}", postId, e);
        }
    }
}
