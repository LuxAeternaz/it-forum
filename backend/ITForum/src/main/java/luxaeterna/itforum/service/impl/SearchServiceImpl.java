package luxaeterna.itforum.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.response.CommentSearchHit;
import luxaeterna.itforum.entity.Comment;
import luxaeterna.itforum.entity.Post;
import luxaeterna.itforum.entity.es.CommentDocument;
import luxaeterna.itforum.entity.es.PostDocument;
import luxaeterna.itforum.mapper.CommentMapper;
import luxaeterna.itforum.mapper.PostMapper;
import luxaeterna.itforum.repository.es.CommentSearchRepository;
import luxaeterna.itforum.repository.es.PostSearchRepository;
import luxaeterna.itforum.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final ElasticsearchOperations esOps;
    private final PostSearchRepository postSearchRepo;
    private final CommentSearchRepository commentSearchRepo;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public SearchServiceImpl(ElasticsearchOperations esOps,
                             PostSearchRepository postSearchRepo,
                             CommentSearchRepository commentSearchRepo,
                             PostMapper postMapper,
                             CommentMapper commentMapper) {
        this.esOps = esOps;
        this.postSearchRepo = postSearchRepo;
        this.commentSearchRepo = commentSearchRepo;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    public PageResult<PostDocument> searchPosts(String keyword, Integer categoryId, int page, int size) {
        try {
            var boolBuilder = new BoolQuery.Builder();

            if (keyword != null && !keyword.isBlank()) {
                boolBuilder.must(MultiMatchQuery.of(mm -> mm
                    .fields("title^3", "content")
                    .query(keyword.trim())
                    .type(TextQueryType.BestFields)
                )._toQuery());
            } else {
                boolBuilder.must(MatchAllQuery.of(m -> m)._toQuery());
            }

            boolBuilder.filter(TermQuery.of(t -> t.field("status").value("PUBLISHED"))._toQuery());
            if (categoryId != null) {
                boolBuilder.filter(TermQuery.of(t -> t.field("categoryId").value(categoryId))._toQuery());
            }

            NativeQuery query = NativeQuery.builder()
                .withQuery(boolBuilder.build()._toQuery())
                .withPageable(PageRequest.of(page - 1, size))
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .build();

            SearchHits<PostDocument> hits = esOps.search(query, PostDocument.class);
            var list = hits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .toList();
            long total = hits.getTotalHits();
            return new PageResult<>(list, total, page, size);
        } catch (Exception e) {
            log.warn("ES unavailable for post search, falling back to MySQL", e);
            return fallbackSearchPosts(keyword, categoryId, page, size);
        }
    }

    @Override
    public PageResult<CommentSearchHit> searchComments(String keyword, int page, int size) {
        try {
            var boolBuilder = new BoolQuery.Builder();

            if (keyword != null && !keyword.isBlank()) {
                boolBuilder.must(MatchQuery.of(m -> m.field("content").query(keyword.trim()))._toQuery());
            } else {
                boolBuilder.must(MatchAllQuery.of(m -> m)._toQuery());
            }

            boolBuilder.filter(TermQuery.of(t -> t.field("status").value("PUBLISHED"))._toQuery());

            NativeQuery query = NativeQuery.builder()
                .withQuery(boolBuilder.build()._toQuery())
                .withPageable(PageRequest.of(page - 1, size))
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .build();

            SearchHits<CommentDocument> hits = esOps.search(query, CommentDocument.class);

            List<Long> postIds = hits.getSearchHits().stream()
                .map(h -> h.getContent().getPostId())
                .distinct()
                .toList();

            Map<Long, String> postTitleMap = new HashMap<>();
            for (Long postId : postIds) {
                postSearchRepo.findById(postId)
                    .ifPresent(pd -> postTitleMap.put(postId, pd.getTitle()));
            }

            var list = hits.getSearchHits().stream().map(h -> {
                CommentDocument cd = h.getContent();
                var ch = new CommentSearchHit();
                ch.setCommentId(cd.getId());
                ch.setContent(cd.getContent());
                ch.setUserId(cd.getUserId());
                ch.setUsername(cd.getUsername());
                ch.setAvatarUrl(cd.getAvatarUrl());
                ch.setPostId(cd.getPostId());
                ch.setPostTitle(postTitleMap.getOrDefault(cd.getPostId(), "(已删除)"));
                ch.setCreatedAt(cd.getCreatedAt());
                return ch;
            }).toList();

            long total = hits.getTotalHits();
            return new PageResult<>(list, total, page, size);
        } catch (Exception e) {
            log.warn("ES unavailable for comment search, falling back to MySQL", e);
            return fallbackSearchComments(keyword, page, size);
        }
    }

    @Override
    public void reindexAll() {
        log.info("Starting full ES reindex...");
        final int BATCH = 500;

        postSearchRepo.deleteAll();
        long postOffset = 0;
        while (true) {
            List<Post> posts = postMapper.selectPublishedBatch(postOffset, BATCH);
            if (posts.isEmpty()) break;
            List<PostDocument> docs = posts.stream()
                .filter(p -> "PUBLISHED".equals(p.getStatus()))
                .map(PostDocument::from)
                .toList();
            if (!docs.isEmpty()) {
                postSearchRepo.saveAll(docs);
            }
            postOffset += BATCH;
        }
        log.info("Reindexed {} posts", postOffset);

        commentSearchRepo.deleteAll();
        long commentOffset = 0;
        while (true) {
            List<Comment> comments = commentMapper.selectPublishedBatch(commentOffset, BATCH);
            if (comments.isEmpty()) break;
            List<CommentDocument> docs = comments.stream()
                .filter(c -> "PUBLISHED".equals(c.getStatus()))
                .map(CommentDocument::from)
                .toList();
            if (!docs.isEmpty()) {
                commentSearchRepo.saveAll(docs);
            }
            commentOffset += BATCH;
        }
        log.info("Reindexed {} comments", commentOffset);
        log.info("ES reindex complete");
    }

    private PageResult<PostDocument> fallbackSearchPosts(String keyword, Integer categoryId, int page, int size) {
        int offset = (page - 1) * size;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<Post> posts = postMapper.search(kw, categoryId, offset, size);
        long total = postMapper.countSearch(kw, categoryId);
        List<PostDocument> docs = posts.stream().map(PostDocument::from).toList();
        return new PageResult<>(docs, total, page, size);
    }

    private PageResult<CommentSearchHit> fallbackSearchComments(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<Comment> comments = commentMapper.search(kw, offset, size);
        long total = commentMapper.countSearch(kw);
        Map<Long, String> postTitleMap = new HashMap<>();
        for (Comment c : comments) {
            if (!postTitleMap.containsKey(c.getPostId())) {
                Post post = postMapper.selectById(c.getPostId());
                postTitleMap.put(c.getPostId(), post != null ? post.getTitle() : "(已删除)");
            }
        }
        List<CommentSearchHit> list = comments.stream().map(c -> {
            var ch = new CommentSearchHit();
            ch.setCommentId(c.getId());
            ch.setContent(c.getContent());
            ch.setUserId(c.getUserId());
            ch.setUsername(c.getUsername());
            ch.setAvatarUrl(c.getAvatarUrl());
            ch.setPostId(c.getPostId());
            ch.setPostTitle(postTitleMap.getOrDefault(c.getPostId(), "(已删除)"));
            ch.setCreatedAt(c.getCreatedAt());
            return ch;
        }).toList();
        return new PageResult<>(list, total, page, size);
    }
}
