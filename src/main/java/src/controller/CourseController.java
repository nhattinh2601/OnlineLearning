package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.service.Category.Dto.CategoryDto;
import src.service.Course.Dto.CourseCreateDto;
import src.service.Course.Dto.CourseDto;
import src.service.Course.Dto.CourseUpdateDto;
import src.service.Course.CourseService;

import java.util.List;
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

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseDto> update(@PathVariable int id, CourseUpdateDto course) {
        return courseService.update(id, course);
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

}
