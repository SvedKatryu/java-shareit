package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.markers.Marker;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public UserDtoResponse create(@Validated({Marker.OnCreate.class}) @RequestBody UserDtoRequest request) {
        log.info("Получен запрос на добавление пользователя {}", request);
        return userClient.create(request);
    }

    @PatchMapping("/{id}")
    public UserDtoResponse update(@PathVariable(value = "id") Long id,
                                  @Validated({Marker.OnUpdate.class}) @RequestBody UserDtoRequest updatedUser) {
        log.info("Получен запрос на редактирование пользователя ID{}", id);
        return userClient.update(id, updatedUser);
    }

    @GetMapping
    public List<UserDtoResponse> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        return userClient.getAll();
    }

    @GetMapping("/{id}")
    public UserDtoResponse getUserById(@PathVariable(value = "id") Long id) {
        log.info("Получен запрос на получение пользователя ID{}", id);
        return userClient.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable() Long id) {
        log.info("Получен запрос на удаление пользователя ID{}", id);
        userClient.delete(id);
    }

}
