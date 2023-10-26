package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Comment;
import src.service.Comment.CommentService;
import src.service.Comment.Dto.CommentCreateDto;
import src.service.Comment.Dto.CommentDto;
import src.service.Comment.Dto.CommentUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/comments")
public class CommentController {
    @Autowired
    private CommentService CommentService;

    @GetMapping()
    public CompletableFuture<List<CommentDto>> findAll() {
        return CommentService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CommentDto> getOne(@PathVariable int id) {
        return CommentService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<CommentDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return CommentService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompletableFuture<Comment>> createComment(@RequestBody CommentCreateDto CommentCreateDto) {
        CompletableFuture<Comment> future = CommentService.createComment(CommentCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(future);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CommentDto> update(@PathVariable int id, CommentUpdateDto Comment) {
        return CommentService.update(id, Comment);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return CommentService.deleteById(id);
    }

    @GetMapping("/{id}/features")
    public CompletableFuture<List<CommentDto>> getCommentFeatures(@PathVariable("id") int parentCommentId) {
        return CommentService.getCommentFeatures(parentCommentId);
    }
}
