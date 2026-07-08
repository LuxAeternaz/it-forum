package luxaeterna.itforum.service;

import luxaeterna.itforum.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Category getById(Integer id);
}
