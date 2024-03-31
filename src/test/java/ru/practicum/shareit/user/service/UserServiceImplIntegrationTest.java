package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDtoRequest userDtoRequest;

    private UserDtoRequest updateDtoRequest;

    @BeforeEach
    void setUp() {
        userDtoRequest = UserDtoRequest.builder()
                .name("username")
                .email("test@email.com")
                .build();
        updateDtoRequest = UserDtoRequest.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();
    }

    @Test
    @DisplayName("Добавление пользователя")
    void create_ShouldReturnUserWithId() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);

        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getId(), greaterThan(0L));
        assertThat(savedUser.getName(), is(userDtoRequest.getName()));
        assertThat(savedUser.getEmail(), is(userDtoRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void update_WithNameAndEmail_ShouldUpdateNameAndEmail() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);
        UserDtoResponse updatedUser = userService.update(savedUser.getId(), updateDtoRequest);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(updateDtoRequest.getName()));
        assertThat(updatedUser.getEmail(), is(updateDtoRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление email пользователя")
    void update_WithOnlyEmail_ShouldUpdateEmail() {
        updateDtoRequest.setName(null);
        UserDtoResponse savedUser = userService.create(userDtoRequest);
        UserDtoResponse updatedUser = userService.update(savedUser.getId(), updateDtoRequest);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateDtoRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление имени пользователя")
    void update_WithOnlyName_ShouldUpdateName() {
        updateDtoRequest.setEmail(null);
        UserDtoResponse savedUser = userService.create(userDtoRequest);
        UserDtoResponse updatedUser = userService.update(savedUser.getId(), updateDtoRequest);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(updateDtoRequest.getName()));
        assertThat(updatedUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Обновление данных пользователя, email и name равны null")
    void update_WithNullEmailAndName_ShouldNotUpdateAnyFields() {
        updateDtoRequest.setEmail(null);
        updateDtoRequest.setName(null);
        UserDtoResponse savedUser = userService.create(userDtoRequest);
        UserDtoResponse updatedUser = userService.update(savedUser.getId(), updateDtoRequest);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    void getUserById_UserFound_ShouldReturnUser() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);

        UserDtoResponse foundUser = userService.getUserById(savedUser.getId());

        assertThat(foundUser.getId(), is(savedUser.getId()));
        assertThat(foundUser.getName(), is(savedUser.getName()));
        assertThat(foundUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Поиск пользователя по id, пользователь не найден")
    void getUserById_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));

        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void getAll_ShouldReturnListOfOne() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);

        List<UserDtoResponse> users = userService.getAll();

        assertThat(users, notNullValue());
        assertThat(users, is(List.of(savedUser)));
    }

    @Test
    @DisplayName("Поиск всех пользователей, когда в БД несколько пользователей")
    void getAll_ShouldReturnUserList() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);
        UserDtoRequest userDto2 = UserDtoRequest.builder()
                .name("username2")
                .email("test2@email.com")
                .build();
        UserDtoResponse savedUser2 = userService.create(userDto2);

        List<UserDtoResponse> users = userService.getAll();

        assertThat(users, notNullValue());
        assertThat(users, is(List.of(savedUser, savedUser2)));
    }

    @Test
    @DisplayName("Поиск всех пользователей, когда в БД нет пользователей")
    void getAll_WithNoUsers_ShouldReturnEmptyList() {

        List<UserDtoResponse> users = userService.getAll();

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }

    @Test
    @DisplayName("Удаление пользователя по id")
    void delete_UserExits_ShouldDeleteUser() {
        UserDtoResponse savedUser = userService.create(userDtoRequest);

        userService.delete(savedUser.getId());
        List<UserDtoResponse> users = userService.getAll();

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }
}
