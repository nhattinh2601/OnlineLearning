package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Rating;
import src.model.User;
import src.service.Rating.Dto.RatingCreateDto;
import src.service.Rating.Dto.RatingDto;
import src.service.Rating.Dto.RatingUpdateDto;
import src.service.Rating.RatingService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/ratings")
public class RatingController {
    @Autowired
    private RatingService ratingService;

    @GetMapping()
    public CompletableFuture<List<RatingDto>> findAll() {
        return ratingService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingDto> getOne(@PathVariable int id) {
        return ratingService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<RatingDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return ratingService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingDto> create(@RequestBody RatingCreateDto input) {
        return ratingService.create(input);
    }

    /*@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingDto> update(@PathVariable int id, RatingUpdateDto rating) {
        return ratingService.update(id, rating);
    }*/
    @PatchMapping("/{userId}")
    public ResponseEntity<Rating> updateRatingField(
            @PathVariable int userId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        Rating updatedRating = ratingService.updateRating(userId, fieldsToUpdate);

        if (updatedRating != null) {
            return ResponseEntity.ok(updatedRating);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return ratingService.deleteById(id);
    }
}
