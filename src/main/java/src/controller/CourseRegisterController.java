package src.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.Dto.CourseRegisterUserDTO;
import src.Dto.ReviewUserDTO;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Course;
import src.model.CourseRegister;
import src.service.CourseRegister.CourseRegisterService;
import src.service.CourseRegister.Dto.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/courseRegisters")
public class CourseRegisterController {
    @Autowired
    private CourseRegisterService courseRegisterService;

    @GetMapping()
    public CompletableFuture<List<CourseRegisterDto>> findAll() {
        return courseRegisterService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseRegisterDto> getOne(@PathVariable int id) {
        return courseRegisterService.getOne(id);
    }



    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CourseRegisterCreateDto courseRegisterCreateDto) {
        return new ResponseEntity<>(courseRegisterService.register(courseRegisterCreateDto), HttpStatus.OK);
    }
    @PutMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestParam String email,
                                                @RequestParam String otp) {
        return new ResponseEntity<>(courseRegisterService.verifyAccount(email, otp), HttpStatus.OK);
    }
    @PutMapping("/regenerate-otp")
    public ResponseEntity<String> regenerateOtp(@RequestParam String email) {
        return new ResponseEntity<>(courseRegisterService.regenerateOtp(email), HttpStatus.OK);
    }

    @PatchMapping("/{courseRegisterId}")
    public ResponseEntity<CourseRegister> updateCourseRegisterField(
            @PathVariable int courseRegisterId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        CourseRegister updatedCourseRegister = courseRegisterService.updateCourseRegister(courseRegisterId, fieldsToUpdate);

        if (updatedCourseRegister != null) {
            return ResponseEntity.ok(updatedCourseRegister);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return courseRegisterService.deleteById(id);
    }
    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<CourseRegisterDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                                   @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return courseRegisterService.findAllPagination(request, size, page * size);
    }



    @GetMapping("/revivewer/{userId}/{courseId}")
    public ResponseEntity<CourseRegisterUserDTO> getCourseRegisterByUser(@PathVariable int userId,@PathVariable int courseId) {
        CourseRegisterUserDTO course = courseRegisterService.getCourseRegisterByUser(userId, courseId);
        if (course != null) {
            return new ResponseEntity<>(course, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RegisterCourseDTO>> getReviewsByUserId(@PathVariable int userId) {
        List<RegisterCourseDTO> register = courseRegisterService.getRegisterCourse(userId);
        return new ResponseEntity<>(register, HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}/students/count")
    public Long countStudentsForCourse(@PathVariable int courseId) {
        return courseRegisterService.countStudentsForCourse(courseId);
    }

    @PutMapping("/confirm-payment/{id}")
    public ResponseEntity<String> regenerateOtp(@PathVariable int id) {
        return new ResponseEntity<>(courseRegisterService.updateOtpSendEmail(id), HttpStatus.OK);
    }

    @GetMapping(value = "/getcoursenoactive")
    public List<UserRegisterCourse> getName() {
        return courseRegisterService.getCourseRegisterNoCheck();
    }

    @PutMapping("/reject-confirm-payment/{id}")
    public ResponseEntity<String> rejectconfirm(@PathVariable int id) {
        return new ResponseEntity<>(courseRegisterService.TuChoiGuiMaXacNhan(id), HttpStatus.OK);
    }

    @PostMapping("/register-course/{userId}/{courseId}/{otp}")
    public ResponseEntity<String> registerCourse(@PathVariable int userId, @PathVariable int courseId,@PathVariable String otp) {
        return new ResponseEntity<>(courseRegisterService.registerCourse(userId,courseId, otp), HttpStatus.OK);
    }

    @PutMapping("/active-course/{id}")
    public ResponseEntity<String> activeCourse(@PathVariable int id) {
        return new ResponseEntity<>(courseRegisterService.activeCourse(id), HttpStatus.OK);
    }

    @GetMapping("/total-sold-in-month/{monthYear}")
    public int getTotalSoldCourseInMonth(@PathVariable String monthYear) {
        return courseRegisterService.totalSoldCourseInMonth(monthYear);
    }

    @GetMapping("/total-price-in-month/{monthYear}")
    public double getTotalPriceCourseInMonth(@PathVariable String monthYear) {
        return courseRegisterService.totalPriceCourseInMonth(monthYear);
    }

    @GetMapping("/total-sold-in-year/{year}")
    public int getTotalSoldCourseInYear(@PathVariable String year) {
        return courseRegisterService.totalSoldCourseInYear(year);
    }
    @GetMapping("/total-sold-in-day/{day}")
    public int getTotalSoldCourseInDay(@PathVariable String day) {
        return courseRegisterService.totalSoldCourseInDay(day);
    }
    @GetMapping("/total-sold")
    public int getTotalSold() {
        return courseRegisterService.totalCourse();
    }

    @GetMapping("/total-price")
    public double getTotalPrice() {
        return courseRegisterService.totalPrice();
    }

    @GetMapping("/total-price-in-day/{day}")
    public double getTotalPriceCourseInDay(@PathVariable String day) {
        return courseRegisterService.totalPriceCourseInDay(day);
    }

    @GetMapping("/total-price-in-day-per-teacher/{day}/{teacherId}")
    public double getTotalPriceCourseInDay(@PathVariable String day, @PathVariable int teacherId) {
        return courseRegisterService.totalPriceCourseInDayPerTeacher(day,teacherId);
    }

    @GetMapping("/total-price-in-month-per-teacher/{day}/{teacherId}")
    public double getTotalPriceCourseInMonth(@PathVariable String day, @PathVariable int teacherId) {
        return courseRegisterService.totalPriceCourseInMonthPerTeacher(day,teacherId);
    }

    @GetMapping("/total-course-in-month-per-teacher/{day}/{teacherId}")
    public double getTotalCourseCourseInMonth(@PathVariable String day, @PathVariable int teacherId) {
        return courseRegisterService.totalCourseInMonthPerTeacher(day,teacherId);
    }

    @GetMapping("/total-course-in-day-per-teacher/{day}/{teacherId}")
    public double getTotalCourseCourseInDay(@PathVariable String day, @PathVariable int teacherId) {
        return courseRegisterService.totalCourseInDayPerTeacher(day,teacherId);
    }

    @GetMapping("/total-course-per-teacher/{teacherId}")
    public double getTotalCoursePerTeacher(@PathVariable int teacherId) {
        return courseRegisterService.totalCoursePerTeacher(teacherId);
    }

    @GetMapping("/total-price-per-teacher/{teacherId}")
    public double getTotalPricePerTeacher(@PathVariable int teacherId) {
        return courseRegisterService.totalPricePerTeacher(teacherId);
    }

    @GetMapping("/total-sold-in-day-no-active/{day}")
    public int getTotalSoldCourseInDayNoActive(@PathVariable String day) {
        return courseRegisterService.totalSoldCourseInDayNoActive(day);
    }

    @GetMapping("/check/{userId}/{courseId}")
    public String checkCourseRegister(@PathVariable int userId, @PathVariable int courseId) {
        if (courseRegisterService.isCourseRegisterValid(userId, courseId)) {
            return "true";
        } else {
            return "false";
        }
    }

    @GetMapping("/total-price-in-time/{begin}/{end}")
    public double getTotalPriceInTime(@PathVariable String begin,@PathVariable String end) {
        return courseRegisterService.totalPriceInTime(begin, end);
    }

    @GetMapping("/total-sold-in-time/{begin}/{end}")
    public double getTotalSoldInTime(@PathVariable String begin,@PathVariable String end) {
        return courseRegisterService.totalSoldInTime(begin, end);
    }

    @GetMapping("/total-price-in-time-per-teacher/{begin}/{end}/{teacherId}")
    public double getTotalPriceInTimePerTeacher(@PathVariable String begin,@PathVariable String end, @PathVariable int teacherId) {
        return courseRegisterService.totalPriceInTimePerTeacher(begin, end, teacherId);
    }

    @GetMapping("/total-sold-in-time-per-teacher/{begin}/{end}/{teacherId}")
    public double getTotalSoldInTimePerTeacher(@PathVariable String begin,@PathVariable String end, @PathVariable int teacherId) {
        return courseRegisterService.totalSoldInTimePerTeacher(begin, end, teacherId);
    }
}
