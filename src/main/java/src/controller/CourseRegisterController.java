package src.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.CourseRegister;
import src.service.Course.Dto.CourseDto;
import src.service.CourseRegister.CourseRegisterService;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;
import src.service.CourseRegister.Dto.CourseRegisterUpdateDto;

import java.util.List;
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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseRegisterDto> create(@RequestBody CourseRegisterCreateDto input) {
        return courseRegisterService.create(input);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CourseRegisterDto> update(@PathVariable int id, CourseRegisterUpdateDto courseRegister) {
        return courseRegisterService.update(id, courseRegister);
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
}
