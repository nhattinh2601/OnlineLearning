package src.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.Dto.CourseRegisterUserDTO;
import src.Dto.RegisterCourseDTO;
import src.Dto.ReviewUserDTO;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Course;
import src.model.CourseRegister;
import src.service.CourseRegister.CourseRegisterService;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;
import src.service.CourseRegister.Dto.CourseRegisterUpdateDto;

import java.util.List;
import java.util.Map;
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

   /* @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseRegisterDto> create(@RequestBody CourseRegisterCreateDto input) {
        return courseRegisterService.create(input);
    }*/

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

    /*@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseRegisterDto> update(@PathVariable int id, CourseRegisterUpdateDto courseRegister) {
        return courseRegisterService.update(id, courseRegister);
    }*/

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


}
