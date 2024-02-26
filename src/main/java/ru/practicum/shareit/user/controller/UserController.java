package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping
    public UserDtoResponse create(@Valid @RequestBody UserDtoRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{id}")
    public User update(@PathVariable(value = "id") Long id,
                       @Valid @RequestBody User updatedUser) {
        return userService.update(id, updatedUser);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(value = "id") Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable() Long id) {
        userService.delete(id);
    }

}
