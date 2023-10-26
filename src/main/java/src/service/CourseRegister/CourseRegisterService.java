package src.service.CourseRegister;

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
import src.model.Course;
import src.model.CourseRegister;
import src.repository.CourseRegisterRepository;
import src.service.Category.Dto.CategoryDto;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;
import src.service.CourseRegister.Dto.CourseRegisterUpdateDto;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CourseRegisterService {
    @Autowired
    private CourseRegisterRepository courseRegisterRepository;
    @Autowired
    private ModelMapper toDto;
    @PersistenceContext
    EntityManager em;
    @Async
    public CompletableFuture<List<CourseRegisterDto>> getAll() {
        return CompletableFuture.completedFuture(
                courseRegisterRepository.findAll().stream().map(
                        x -> toDto.map(x, CourseRegisterDto.class)
                ).collect(Collectors.toList()));
    }
    @Async
    public CompletableFuture<CourseRegisterDto> getOne(int id) {
        CourseRegister courseRegister = courseRegisterRepository.findById(id).orElse(null);
        if (courseRegister == null) {
            throw new NotFoundException("Không tìm thấy quyền với ID " + id);
        }
        return CompletableFuture.completedFuture(toDto.map(courseRegister, CourseRegisterDto.class));
    }


    @Async
    public CompletableFuture<CourseRegisterDto> create(CourseRegisterCreateDto input) {
        CourseRegister courseRegister = new CourseRegister();
        courseRegister.setCourseId(input.getCourseId());
        courseRegister.setUserId(input.getUserId());

        CourseRegister savedCourseRegister = courseRegisterRepository.save(courseRegister);
        return CompletableFuture.completedFuture(toDto.map(savedCourseRegister, CourseRegisterDto.class));
    }

    @Async
    public CompletableFuture<CourseRegisterDto> update(int id, CourseRegisterUpdateDto courseRegisters) {
        CourseRegister existingCourseRegister = courseRegisterRepository.findById(id).orElse(null);
        if (existingCourseRegister == null)
            throw new NotFoundException("Unable to find course!");
        BeanUtils.copyProperties(courseRegisters, existingCourseRegister);
        return CompletableFuture.completedFuture(toDto.map(courseRegisterRepository.save(existingCourseRegister), CourseRegisterDto.class));
    }

    @Async
    public CompletableFuture<String> deleteById(int id) {
        Optional<CourseRegister> courseRegisterOptional = courseRegisterRepository.findById(id);
        if (!courseRegisterOptional.isPresent()) {
            return CompletableFuture.completedFuture("Không có ID này");
        }
        try {
            CourseRegister courseRegister = courseRegisterOptional.get();
            courseRegister.setIsDeleted(true);
            courseRegister.setUpdateAt(new Date(new java.util.Date().getTime()));
            courseRegisterRepository.save(courseRegister);
            return CompletableFuture.completedFuture("Đánh dấu xóa thành công");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Xóa không được");
        }
    }

    @Async
    public CompletableFuture<PagedResultDto<CourseRegisterDto>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip) {
        long total = courseRegisterRepository.count();
        Pagination pagination = Pagination.create(total, skip, limit);

        ApiQuery<CourseRegister> features = new ApiQuery<>(request, em, CourseRegister.class, pagination);
        return CompletableFuture.completedFuture(PagedResultDto.create(pagination,
                features.filter().orderBy().paginate().exec().stream().map(x -> toDto.map(x, CourseRegisterDto.class)).toList()));
    }
}

