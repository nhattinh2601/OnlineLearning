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

import src.model.Cart;
import src.service.Cart.CartService;
import src.service.Cart.Dto.CartDto;
import src.service.Cart.Dto.CartUpdateDto;


import java.util.List;

import java.util.Map;
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

    @Authenticate
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CartDto> create() {
        int userId = ((int) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getAttribute("id")));
        return cartService.create(userId);
    }

   /* @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CartDto> update(@PathVariable int id, CartUpdateDto cart) {
        return cartService.update(id, cart);
    }*/
   @PatchMapping("/{cartId}")
   public ResponseEntity<Cart> updateCartField(
           @PathVariable int cartId,
           @RequestBody Map<String, Object> fieldsToUpdate) {

       Cart updatedCart = cartService.updateCart(cartId, fieldsToUpdate);

       if (updatedCart != null) {
           return ResponseEntity.ok(updatedCart);
       } else {
           return ResponseEntity.notFound().build();
       }
   }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return cartService.deleteById(id);
    }

    
}
