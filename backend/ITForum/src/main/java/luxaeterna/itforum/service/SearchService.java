package luxaeterna.itforum.service;

import luxaeterna.itforum.common.PageResult;
import luxaeterna.itforum.dto.response.CommentSearchHit;
import luxaeterna.itforum.entity.es.PostDocument;

public interface SearchService {
    PageResult<PostDocument> searchPosts(String keyword, Integer categoryId, int page, int size);

    PageResult<CommentSearchHit> searchComments(String keyword, int page, int size);

    void reindexAll();
}
