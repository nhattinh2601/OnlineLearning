package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.service.Review.ReviewService;
import src.service.Review.Dto.ReviewCreateDto;
import src.service.Review.Dto.ReviewDto;
import src.service.Review.Dto.ReviewUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping()
    public CompletableFuture<List<ReviewDto>> findAll() {
        return reviewService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ReviewDto> getOne(@PathVariable int id) {
        return reviewService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<ReviewDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                        @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return reviewService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ReviewDto> create(@RequestBody ReviewCreateDto input) {
        return reviewService.create(input);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ReviewDto> update(@PathVariable int id, ReviewUpdateDto review) {
        return reviewService.update(id, review);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return reviewService.deleteById(id);
    }
}
