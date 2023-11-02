package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.service.Document.Dto.DocumentCreateDto;
import src.service.Document.Dto.DocumentDto;
import src.service.Document.Dto.DocumentUpdateDto;
import src.service.Document.DocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/documents")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping()
    public CompletableFuture<List<DocumentDto>> findAll() {
        return documentService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<DocumentDto> getOne(@PathVariable int id) {
        return documentService.getOne(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<DocumentDto> create(@RequestBody DocumentCreateDto input) {
        return documentService.create(input);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<DocumentDto> update(@PathVariable int id, DocumentUpdateDto document) {
        return documentService.update(id, document);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return documentService.deleteById(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<DocumentDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                             @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return documentService.findAllPagination(request, size, page * size);
    }

}
