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
import src.model.Rating;
import src.repository.CourseRegisterRepository;
import src.repository.CourseRepository;
import src.repository.RatingRepository;
import src.service.Category.Dto.CategoryDto;
import src.service.Course.Dto.CourseCreateDto;
import src.service.Course.Dto.CourseDto;
import src.service.Course.Dto.CourseUpdateDto;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseRegisterRepository courseRegisterRepository;
    @Autowired
    private RatingRepository ratingRepository;

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
        course.setTitle(input.getTitle());
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

    @Async
    public double calculateCourseRating(int courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return -1;
        }

        Iterable<Rating> ratings = ratingRepository.findByCourseId(courseId);

        int totalRatings = 0;
        double totalRatingValue = 0.0;

        for (Rating rating : ratings) {
            totalRatings++;
            totalRatingValue += rating.getRating();
        }

        if (totalRatings == 0) {
            return 0;
        }

        double averageRating = totalRatingValue / totalRatings;

        return averageRating;
    }

    @Async
    public CompletableFuture<List<CourseDto>> getTopNew() {
        List<Course> newestCourses = courseRepository.findAll()
                .stream()
                .filter(course -> course.getActive() || !course.getIsDeleted())
                .sorted(Comparator.comparing(Course::getCreateAt).reversed())
                .limit(4)
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(newestCourses.stream()
                .map(course -> toDto.map(course, CourseDto.class))
                .collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<List<CourseDto>> getTopMost() {
        List<Course> courses = courseRepository.findAll();

        Map<Course, Long> registrationsCountMap = courses.stream()
                .filter(course -> course.getActive() || !course.getIsDeleted())
                .collect(Collectors.toMap(
                        course -> course,
                        course -> courseRegisterRepository.countByCourseIdAndIsActive(course.getId(), true)
                ));

        // Sort courses by registration count in descending order and get top 4
        List<Course> top4Courses = registrationsCountMap.entrySet().stream()
                .sorted(Map.Entry.<Course, Long>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(top4Courses.stream()
                .map(course -> toDto.map(course, CourseDto.class))
                .collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<List<CourseDto>> searchByTitle(String title) {
        List<Course> foundCourses = courseRepository.searchByTitle(title);

        return CompletableFuture.completedFuture(foundCourses.stream()
                .map(course -> toDto.map(course, CourseDto.class))
                .collect(Collectors.toList()));
    }

    public CompletableFuture<List<CourseDto>> getCoursesByCategoryId(int categoryId) {
        List<Course> foundCourses = courseRepository.findByCategoryId(categoryId);
        return CompletableFuture.completedFuture(foundCourses.stream()
                .map(course -> toDto.map(course, CourseDto.class))
                .collect(Collectors.toList()));

    }

}
