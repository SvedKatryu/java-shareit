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
public class UserController {

    private final UserServiceImpl service;

    @PostMapping
    public UserDtoResponse create(@Validated @RequestBody UserDtoRequest request) {
        return service.create(request);
    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        return service.update(updatedUser);
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(value = "id") Long id) {
        return service.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable() Long id) {
        service.delete(id);
    }

}
