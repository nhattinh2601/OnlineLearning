package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import src.config.annotation.ApiPrefixController;
import src.config.annotation.Authenticate;
import src.config.dto.PagedResultDto;
import src.model.Review;
import src.model.User;
import src.service.Review.ReviewService;
import src.service.Review.Dto.ReviewCreateDto;
import src.service.Review.Dto.ReviewDto;
import src.service.Review.Dto.ReviewUpdateDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Authenticate
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    @Tag(name = "useraddresss", description = "Operations related to useraddresss")
//    @Operation(summary = "Hello API")
    public CompletableFuture<ReviewDto> create() {
        int userId = ((int) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getAttribute("id")));
        return reviewService.create(userId);
    }

    /*@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ReviewDto> update(@PathVariable int id, ReviewUpdateDto review) {
        return reviewService.update(id, review);
    }*/

    @PatchMapping("/{userId}")
    public ResponseEntity<Review> updateReviewField(
            @PathVariable int userId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        Review updatedReview = reviewService.updateReview(userId, fieldsToUpdate);

        if (updatedReview != null) {
            return ResponseEntity.ok(updatedReview);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return reviewService.deleteById(id);
    }
}
