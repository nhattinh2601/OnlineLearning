package src.service.Course;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import src.config.dto.PagedResultDto;
import src.config.dto.Pagination;
import src.config.exception.NotFoundException;
import src.config.utils.ApiQuery;
import src.model.Category;
import src.model.Course;
import src.repository.CourseRepository;
import src.service.Category.Dto.CategoryDto;
import src.service.Course.Dto.CourseCreateDto;
import src.service.Course.Dto.CourseDto;
import src.service.Course.Dto.CourseUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ModelMapper toDto;
    @PersistenceContext
    EntityManager em;
    @Async
    public CompletableFuture<List<CourseDto>> getAll() {
        return CompletableFuture.completedFuture(
                courseRepository.findAll().stream().map(
                        x -> toDto.map(x, CourseDto.class)
                ).collect(Collectors.toList()));
    }
    @Async
    public CompletableFuture<CourseDto> getOne(int id) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(course, CourseDto.class));
    }


    @Async
    public CompletableFuture<CourseDto> create(CourseCreateDto input) {
        Course course = new Course();
        course.setPrice(input.getPrice());
        course.setPromotional_price(input.getPromotional_price());
        course.setSold(input.getSold());
        course.setDescription(input.getDescription());
        course.setActive(input.getActive());
        course.setRating(input.getRating());
        course.setImage(input.getImage());
        course.setCategoryId(input.getCategoryId());
        course.setUserId(input.getUserId());

        Course savedCourse = courseRepository.save(course);
        return CompletableFuture.completedFuture(toDto.map(savedCourse, CourseDto.class));
    }

    @Async
    public CompletableFuture<CourseDto> update(int id, CourseUpdateDto courses) {
        Course existingCourse = courseRepository.findById(id).orElse(null);
        if (existingCourse == null)
            throw new NotFoundException("Unable to find course!");
        BeanUtils.copyProperties(courses, existingCourse);
        return CompletableFuture.completedFuture(toDto.map(courseRepository.save(existingCourse), CourseDto.class));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<Course> courseOptional = courseRepository.findById(id);
        if (!courseOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            Course course = courseOptional.get();
            course.setIsDeleted(true);
            course.setUpdateAt(new Date(new java.util.Date().getTime()));
            courseRepository.save(course);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

    @Async
    public CompletableFuture<PagedResultDto<CourseDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = courseRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<Category> features = new ApiQuery<>(request, em, Category.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, CourseDto.class)).toList()));
    }

}
