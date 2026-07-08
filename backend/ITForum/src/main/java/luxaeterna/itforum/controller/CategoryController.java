package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.service.CategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<?> listAll() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(categoryService.getById(id));
    }
}
