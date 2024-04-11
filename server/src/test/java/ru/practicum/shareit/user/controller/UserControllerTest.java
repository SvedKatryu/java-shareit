package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserServiceImpl userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDtoRequest userDtoRequest;
    private UserDtoResponse userDtoResponse;

    private long userId;

    @BeforeEach
    void setUp() {
        userDtoRequest = UserDtoRequest.builder()
                .name("name")
                .email("test@mail.com")
                .build();
        userDtoResponse = UserDtoResponse.builder()
                .name("name")
                .email("test@mail.com")
                .build();
        userId = 1;
    }

    @Test
    @DisplayName("Добавление пользователя")
    @SneakyThrows
    void addUser_ValidUser_ShouldReturnDto() {
        when(userService.create(userDtoRequest))
                .thenReturn(userDtoResponse);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(jsonPath("$.id", is(userDtoRequest.getId())))
                .andExpect(jsonPath("$.name", is(userDtoRequest.getName())))
                .andExpect(jsonPath("$.email", is(userDtoRequest.getEmail())));

        verify(userService, times(1)).create(userDtoRequest);
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    @SneakyThrows
    void update_ShouldReturnUserDto() {
        when(userService.update(userId, userDtoRequest))
                .thenReturn(userDtoResponse);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(jsonPath("$.id", is(userDtoRequest.getId())))
                .andExpect(jsonPath("$.name", is(userDtoRequest.getName())))
                .andExpect(jsonPath("$.email", is(userDtoRequest.getEmail())));

        verify(userService, times(1)).update(userId, userDtoRequest);
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    @SneakyThrows
    void getUserById_shouldReturnUserDto() {
        when(userService.getUserById(userId))
                .thenReturn(userDtoResponse);

        mvc.perform(get("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(jsonPath("$.id", is(userDtoRequest.getId())))
                .andExpect(jsonPath("$.name", is(userDtoRequest.getName())))
                .andExpect(jsonPath("$.email", is(userDtoRequest.getEmail())));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    @SneakyThrows
    void getAll_ShouldReturnListOfUserDto() {
        when(userService.getAll())
                .thenReturn(List.of(userDtoResponse));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(userDtoRequest))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(userDtoRequest.getId())))
                .andExpect(jsonPath("$.[0].name", is(userDtoRequest.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDtoRequest.getEmail())));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Удаление пользователя по id")
    @SneakyThrows
    void delete_ShouldReturnOkStatus() {
        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(userId);
    }
}
