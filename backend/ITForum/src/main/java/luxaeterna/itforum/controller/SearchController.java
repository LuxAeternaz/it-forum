package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.dto.response.SearchResult;
import luxaeterna.itforum.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/posts")
    public Result<?> searchPosts(@RequestParam(required = false, defaultValue = "") String q,
                                  @RequestParam(required = false) Integer categoryId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return Result.success(searchService.searchPosts(q, categoryId, page, size));
    }

    @GetMapping("/comments")
    public Result<?> searchComments(@RequestParam(required = false, defaultValue = "") String q,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return Result.success(searchService.searchComments(q, page, size));
    }

    @GetMapping
    public Result<?> search(@RequestParam(required = false, defaultValue = "") String q,
                            @RequestParam(required = false) Integer categoryId,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size) {
        var result = new SearchResult();
        result.setPosts(searchService.searchPosts(q, categoryId, page, size));
        result.setComments(searchService.searchComments(q, page, size));
        return Result.success(result);
    }

    @PostMapping("/reindex")
    public Result<?> reindex() {
        searchService.reindexAll();
        return Result.success("全量索引重建完成");
    }
}
