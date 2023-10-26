package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.service.Feedback.Dto.FeedbackCreateDto;
import src.service.Feedback.Dto.FeedbackDto;
import src.service.Feedback.Dto.FeedbackUpdateDto;
import src.service.Feedback.FeedbackService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/feedbacks")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping()
    public CompletableFuture<List<FeedbackDto>> findAll() {
        return feedbackService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<FeedbackDto> getOne(@PathVariable int id) {
        return feedbackService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<FeedbackDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return feedbackService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<FeedbackDto> create(@RequestBody FeedbackCreateDto input) {
        return feedbackService.create(input);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<FeedbackDto> update(@PathVariable int id, FeedbackUpdateDto feedback) {
        return feedbackService.update(id, feedback);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return feedbackService.deleteById(id);
    }
}
