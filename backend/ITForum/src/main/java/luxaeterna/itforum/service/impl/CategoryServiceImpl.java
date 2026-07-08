package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.entity.Category;
import luxaeterna.itforum.exception.ResourceNotFoundException;
import luxaeterna.itforum.mapper.CategoryMapper;
import luxaeterna.itforum.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category getById(Integer id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("版块不存在");
        }
        return category;
    }
}
