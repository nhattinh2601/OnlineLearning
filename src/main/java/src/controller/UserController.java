package src.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.annotation.ApiPrefixController;
import src.model.User;
import src.service.User.Dto.UserCreateDto;
import src.service.User.Dto.UserDto;
import src.service.User.Dto.UserUpdateDto;
import src.service.User.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController(value = "/users")
public class UserController {
    @Autowired
    private UserService userService;


    @GetMapping( "/{id}")
    public CompletableFuture<UserDto> findOneById(@PathVariable int id) {
        return userService.getOne(id);
    }

    @GetMapping()
    public CompletableFuture<List<UserDto>> findAll() {
        return userService.getAll();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserDto> create(@RequestBody UserCreateDto input) {
        return userService.create(input);
    }

    /*@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserDto> update(@PathVariable int id, UserUpdateDto user) {
        return userService.update(id, user);
    }*/

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUserField(
            @PathVariable int userId,
            @RequestBody Map<String, Object> fieldsToUpdate) {

        User updatedUser = userService.updateUser(userId, fieldsToUpdate);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> deleteById(@PathVariable int id) {
        return userService.deleteById(id);
    }
}
