package src.service.Category;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import src.config.dto.PagedResultDto;
import src.config.dto.Pagination;
import src.config.exception.NotFoundException;
import src.config.utils.ApiQuery;
import src.model.Category;
import src.repository.CategoryRepository;
import src.service.Category.Dto.CategoryCreateDto;
import src.service.Category.Dto.CategoryDto;
import src.service.Category.Dto.CategoryUpdateDto;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.sql.Types.NULL;


@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper toDto;

    @PersistenceContext
    EntityManager em;

    @Async
    public CompletableFuture<List<CategoryDto>> getAll() {
        return CompletableFuture.completedFuture(
                categoryRepository.findAll().stream().map(
                        x -> toDto.map(x, CategoryDto.class)
                ).collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<CategoryDto> getOne(int id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(category, CategoryDto.class));
    }

    public CompletableFuture<Category> createCategory(CategoryCreateDto categoryCreateDto) {
        CompletableFuture<Category> future = new CompletableFuture<>();

        Category newCategory = new Category();
        newCategory.setName(categoryCreateDto.getName());
        newCategory.setImage(categoryCreateDto.getImage());
        newCategory.setParentCategoryId(categoryCreateDto.getParentCategoryId());

        // Bạn có thể thiết lập các thuộc tính khác của đối tượng Category ở đây.

        Category savedCategory = categoryRepository.save(newCategory);
        future.complete(savedCategory);

        return future;
    }

    @Async
    public CompletableFuture<CategoryDto> update(int id, CategoryUpdateDto categorys) {
        Category existingCategory = categoryRepository.findById(id).orElse(null);
        if (existingCategory == null)
            throw new NotFoundException("Unable to find category!");
        BeanUtils.copyProperties(categorys, existingCategory);
        existingCategory.setUpdateAt(new Date(new java.util.Date().getTime()));
        return CompletableFuture.completedFuture(toDto.map(categoryRepository.save(existingCategory), CategoryDto.class));
    }

    @Async
    public CompletableFuture<PagedResultDto<CategoryDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = categoryRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Category> features = new ApiQuery<>(request, em, Category.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, CategoryDto.class)).toList()));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (!categoryOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Category category = categoryOptional.get();
            category.setIsDeleted(true);
            category.setUpdateAt(new Date(new java.util.Date().getTime()));
            categoryRepository.save(category);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

    @Async
    public CompletableFuture<List<CategoryDto>> getCategoryFeatures(int parentCategoryId) {
        if (parentCategoryId == NULL) {
            throw new IllegalArgumentException("Parent category ID cannot be null");
        }

        List<Category> categories = categoryRepository.findAllByParentCategoryId(parentCategoryId);
        if (categories == null) {
            throw new NotFoundException("Unable to find categories with parent category ID " + parentCategoryId);
        }
        return CompletableFuture.completedFuture(categories.stream().map(
                x -> toDto.map(x, CategoryDto.class)
        ).collect(Collectors.toList()));
    }
}
