package src.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.config.dto.PagedResultDto;
import src.model.Cart;
import src.service.Cart.CartService;
import src.service.Cart.Dto.CartCreateDto;
import src.service.Cart.Dto.CartDto;
import src.service.Cart.Dto.CartUpdateDto;
import src.service.CourseRegister.Dto.CourseRegisterCreateDto;
import src.service.CourseRegister.Dto.CourseRegisterDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/carts")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping()
    public CompletableFuture<List<CartDto>> findAll() {
        return cartService.getAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CartDto> getOne(@PathVariable int id) {
        return cartService.getOne(id);
    }

    @GetMapping("/pagination")
    public CompletableFuture<PagedResultDto<CartDto>> findAllPagination(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") Integer page ,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false, defaultValue = "createAt") String orderBy) {
        return cartService.findAllPagination(request, size, page * size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CartDto> create(@RequestBody CartCreateDto input) {
        return cartService.create(input);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CartDto> update(@PathVariable int id, CartUpdateDto cart) {
        return cartService.update(id, cart);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return cartService.deleteById(id);
    }

    
}
