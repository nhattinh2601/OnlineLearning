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
import src.Dto.CourseRegisterUserDTO;
import src.Dto.RegisterCourseDTO;
import src.Dto.ReviewUserDTO;
import src.config.dto.PagedResultDto;
import src.config.dto.Pagination;
import src.config.exception.NotFoundException;
import src.config.gmail.EmailUtil;
import src.config.gmail.OtpUtil;
import src.config.utils.ApiQuery;
import src.model.*;
import src.repository.CourseRegisterRepository;
import src.repository.CourseRepository;
import src.repository.UserRepository;
import src.service.Category.Dto.CategoryDto;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;
import src.service.CourseRegister.Dto.CourseRegisterUpdateDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CourseRegisterService {
    @Autowired
    private CourseRegisterRepository courseRegisterRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
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
            // Check if there is an existing registration for the given courseRegisterId and courseId
            Optional<CourseRegister> existingRegistration = courseRegisterRepository.findByUserIdAndCourseId(
                    courseRegisterDto.getUserId(), courseRegisterDto.getCourseId());

            if (existingRegistration.isPresent()) {
                return "User is already registered for this course";
            }

            // If not already registered, generate OTP and proceed with registration
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


    /*@Async
    public CompletableFuture<CourseRegisterDto> update(int id, CourseRegisterUpdateDto courseRegisters) {
        CourseRegister existingCourseRegister = courseRegisterRepository.findById(id).orElse(null);
        if (existingCourseRegister == null)
            throw new NotFoundException("Unable to find course!");
        BeanUtils.copyProperties(courseRegisters, existingCourseRegister);
        return CompletableFuture.completedFuture(toDto.map(courseRegisterRepository.save(existingCourseRegister), CourseRegisterDto.class));
    }*/

    public CourseRegister updateCourseRegister(int courseRegisterId, Map<String, Object> fieldsToUpdate) {
        Optional<CourseRegister> optionalCourseRegister = courseRegisterRepository.findById(courseRegisterId);

        if (optionalCourseRegister.isPresent()) {
            CourseRegister courseRegister = optionalCourseRegister.get();
            updateCourseRegisterFields(courseRegister, fieldsToUpdate);
            courseRegister.setUpdateAt(new Date());
            courseRegisterRepository.save(courseRegister);
            return courseRegister;
        }

        return null;
    }

    private void updateCourseRegisterFields(CourseRegister courseRegister, Map<String, Object> fieldsToUpdate) {
        for (Map.Entry<String, Object> entry : fieldsToUpdate.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            updateCourseRegisterField(courseRegister, fieldName, value);
        }
    }

    private void updateCourseRegisterField(CourseRegister courseRegister, String fieldName, Object value) {
        switch (fieldName) {
            case "otp":
                courseRegister.setOtp((String) value);
                break;
            case "courseId":
                courseRegister.setCourseId((int) value);
                break;
            case "userId":
                courseRegister.setUserId((int) value);
                break;

            default:
                break;
        }
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



    public CourseRegisterUserDTO getCourseRegisterByUser(int user, int course) {
        Optional<CourseRegister> optional = courseRegisterRepository.findByUserIdAndCourseId(user, course);
        if (optional.isPresent()) {
            CourseRegister courseRegister = optional.get();

            CourseRegisterUserDTO dto = new CourseRegisterUserDTO();
            dto.setUserId(courseRegister.getUserByUserId().getId());
            dto.setCourseId(courseRegister.getCourseByCourseId().getId());
            dto.setUsercourseId(courseRegister.getCourseByCourseId().getUserId());
            dto.setActive(courseRegister.getCourseByCourseId().getActive());
            dto.setIsActive(courseRegister.getIsActive());


            return dto;
        }

        // Trường hợp không tìm thấy review với reviewId
        return null;
    }

    public List<RegisterCourseDTO> getRegisterCourse(int userId) {
        List<RegisterCourseDTO> result = new ArrayList<>();

        List<CourseRegister> courseRegisters = courseRegisterRepository.findByUserId(userId);

        for (CourseRegister courseRegister : courseRegisters) {
            RegisterCourseDTO dto = new RegisterCourseDTO();
            dto.setCourseId(courseRegister.getCourseId());
            dto.setUserId(courseRegister.getUserId());
            dto.setTitle(courseRegister.getCourseByCourseId().getTitle());
            dto.setImage(courseRegister.getCourseByCourseId().getImage());
            dto.setName(courseRegister.getCourseByCourseId().getUserByUserId().getFullname());
            dto.setActive(courseRegister.getCourseByCourseId().getActive());
            dto.setIsActive(courseRegister.getIsActive());
            dto.setDeleted(courseRegister.getCourseByCourseId().getIsDeleted());
            dto.setIsDeleted(courseRegister.getIsDeleted());

            result.add(dto);
        }

        return result;
    }

    public Long countStudentsForCourse(int courseId) {
        return courseRegisterRepository.countDistinctUsersByCourseId(courseId);
    }



}

