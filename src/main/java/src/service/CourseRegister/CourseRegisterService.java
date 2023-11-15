package src.service.CourseRegister;

import jakarta.mail.MessagingException;
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
import src.config.gmail.EmailUtil;
import src.config.gmail.OtpUtil;
import src.config.utils.ApiQuery;
import src.model.Category;
import src.model.Course;
import src.model.CourseRegister;
import src.model.User;
import src.repository.CourseRegisterRepository;
import src.service.Category.Dto.CategoryDto;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;
import src.service.CourseRegister.Dto.CourseRegisterUpdateDto;

import java.time.Duration;
import java.time.LocalDateTime;
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
    @Autowired
    private OtpUtil otpUtil;
    @Autowired
    private EmailUtil emailUtil;
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


    /*@Async
    public CompletableFuture<CourseRegisterDto> create(CourseRegisterCreateDto input) {
        CourseRegister courseRegister = new CourseRegister();
        courseRegister.setCourseId(input.getCourseId());
        courseRegister.setUserId(input.getUserId());

        CourseRegister savedCourseRegister = courseRegisterRepository.save(courseRegister);
        return CompletableFuture.completedFuture(toDto.map(savedCourseRegister, CourseRegisterDto.class));
    }*/


    public String register(CourseRegisterCreateDto courseRegisterDto) {
        try {
            String otp = otpUtil.generateOtp();
            emailUtil.sendOtpEmail(courseRegisterDto.getEmail(), otp);

            CourseRegister courseRegister = new CourseRegister();
            courseRegister.setCourseId(courseRegisterDto.getCourseId());
            courseRegister.setUserId(courseRegisterDto.getUserId());
            courseRegister.setOtp(otp);
            courseRegister.setOtpGeneratedTime(LocalDateTime.now());

            courseRegisterRepository.save(courseRegister);

            return "User registration successful";
        } catch (Exception e) {
            // Log the exception or handle it as appropriate for your application
            e.printStackTrace();
            return "User registration failed";
        }
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

    public String verifyAccount(String email, String otp) {
        CourseRegister courseRegister = null;
        if (courseRegister.getOtp().equals(otp) && Duration.between(courseRegister.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds() < (1 * 60)) {
            courseRegister.setIsActive(true);
            courseRegisterRepository.save(courseRegister);
            return "OTP verified you can login";
        }
        return "Please regenerate otp and try again";
    }

    public String regenerateOtp(String email) {
        CourseRegister courseRegister = null;
        String otp = otpUtil.generateOtp();
        emailUtil.sendOtpEmail(email, otp);
        courseRegister.setOtp(otp);
        courseRegister.setOtpGeneratedTime(LocalDateTime.now());
        courseRegisterRepository.save(courseRegister);
        return "Email sent... please verify account within 1 minute";
    }

}

