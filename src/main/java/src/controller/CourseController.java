package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Course;
import src.service.Category.Dto.CategoryDto;
import src.service.Course.Dto.CourseCreateDto;
import src.service.Course.Dto.CourseDto;
import src.service.Course.Dto.CourseInfoDTO;
import src.service.Course.Dto.CourseUpdateDto;
import src.service.Course.CourseService;
import src.service.Video.Dto.VideoDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping()
    public CompletableFuture<List<CourseDto>> findAll() {
        return courseService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseDto> getOne(@PathVariable int id) {
        return courseService.getOne(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseDto> create(@RequestBody CourseCreateDto input) {
        return courseService.create(input);
    }


    @PatchMapping("/{courseId}")
    public ResponseEntity<Course> updateCourseField(
            @PathVariable int courseId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        Course updatedCourse = courseService.updateCourse(courseId, fieldsToUpdate);

        if (updatedCourse != null) {
            return ResponseEntity.ok(updatedCourse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return courseService.deleteById(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<CourseDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return courseService.findAllPagination(request, size, page * size);
    }
    @GetMapping("/calculateCourseRating")
    public double calculateCourseRating(@RequestParam int courseId) {
        return courseService.calculateCourseRating(courseId);
    }

    @GetMapping("/topNew")
    public CompletableFuture<List<CourseDto>> getTopNew() {
        return courseService.getTopNew();
    }

    @GetMapping("/topMost")
    public CompletableFuture<List<CourseDto>> getTopMost() {
        return courseService.getTopMost();
    }

    @GetMapping("/search/{title}")
    public CompletableFuture<List<CourseDto>> searchCoursesByTitle(@PathVariable String title) {
        return courseService.searchByTitle(title);
    }

    @GetMapping("/searchCategory/{categoryId}")
    public CompletableFuture<List<CourseDto>> getCoursesByCategoryId(@PathVariable int categoryId) {
        return courseService.getCoursesByCategoryId(categoryId);
    }

    @GetMapping("/user={userId}")
    public CompletableFuture<List<CourseDto>> findByCourseId(@PathVariable int userId) {
        return courseService.findByUserId(userId);
    }

    @GetMapping(value = "/getCourseRelateInfo/{id}")
    public List<CourseInfoDTO> getCourseRelateInfo(@PathVariable int id) {
        return courseService.getCourseAndRelateInfo(id);
    }

    @GetMapping(value = "/get4CourseNewRelateInfo")
    public List<CourseInfoDTO> get4CourseNewRelateInfo() {
        return courseService.get4CourseNewAndRelateInfo();
    }
    @GetMapping(value = "/get4CourseRatingRelateInfo")
    public List<CourseInfoDTO> get4CourseRatingRelateInfo() {
        return courseService.get4CourseRatingAndRelateInfo();
    }

    @GetMapping(value = "/get4CourseSoldRelateInfo")
    public List<CourseInfoDTO> get4CourseSoldRelateInfo() {
        return courseService.get4CourseSoldAndRelateInfo();
    }

    @GetMapping("/findCouseAndRelateInfoByTitle/{title}")
    public List<CourseInfoDTO> findCoursesAndRelateInfoByTitle(@PathVariable String title) {
        return courseService.findCourseSoldAndRelateInfoByTitle(title);
    }

    @GetMapping("/sortCourseInCategory/{categoryId}/sort_by={sortName}")
    public List<CourseInfoDTO> sortCourseInCategory(@PathVariable int categoryId, @PathVariable String sortName) {
        return courseService.sortCourseInCategory(categoryId, sortName);
    }

    @GetMapping("/getCoursesAndRelateInfo")
    public List<CourseInfoDTO> getCoursesAndRelateInfo() {
        return courseService.getCoursesAndRelateInfo();
    }

    @PatchMapping("/unlock-course/{id}")
    public ResponseEntity<String> unlockCourse(@PathVariable int id) {
        return new ResponseEntity<>(courseService.unLockCourse(id), HttpStatus.OK);
    }

    @PatchMapping("/lock-course/{id}")
    public ResponseEntity<String> lockCourse(@PathVariable int id) {
        return new ResponseEntity<>(courseService.lockCourse(id), HttpStatus.OK);
    }
}
