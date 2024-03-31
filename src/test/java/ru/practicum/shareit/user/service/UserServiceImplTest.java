package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JpaUserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User user;

    private User updatedUser;

    private UserDtoRequest userDtoRequest;

    private UserDtoResponse userDtoResponse;

    private long userId;

    @BeforeEach
    void setUp() {
        user = new User();
        userDtoRequest = UserDtoRequest.builder()
                .name("name")
                .email("test@mail.com")
                .build();
        userDtoResponse = UserDtoResponse.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();
        updatedUser = User.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();
        userId = 1;
    }

    @Test
    @DisplayName("Добавление пользователя")
    void create_ValidUser_ShouldReturnUserDto() {
        when(userMapper.toUser(userDtoRequest))
                .thenReturn(user);
        when(userRepository.save(user))
                .thenReturn(user);

        userService.create(userDtoRequest);

        verify(userMapper, times(1)).toUser(userDtoRequest);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void update_UserFoundAndNameNotNullEmailNotNull_ShouldUpdateNameAndEmail() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(userDtoResponse);

        userService.update(userId, userDtoRequest);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(userDtoRequest.getName()));
        assertThat(savedUser.getEmail(), is(userDtoRequest.getEmail()));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(savedUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    @DisplayName("Обновление данных пользователя, пользователь не найден")
    void update_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.update(userId, userDtoRequest));
        assertThat(e.getMessage(), is("Пользователь с ID" + userId + " не найден"));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Обновление email пользователя")
    void update_UserFoundAndNameNullEmailNotNull_ShouldUpdateOnlyEmail() {
        userDtoRequest.setName(null);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(userDtoResponse);

        userService.update(userId, userDtoRequest);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(userDtoRequest.getName()));
        assertThat(savedUser.getEmail(), is(userDtoRequest.getEmail()));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(savedUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    @DisplayName("Обновление имени пользователя")
    void update_UserFoundAndNameNotNullEmailNull_ShouldUpdateOnlyName() {
        userDtoRequest.setEmail(null);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(userDtoResponse);

        userService.update(userId, userDtoRequest);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(userDtoRequest.getName()));
        assertThat(savedUser.getEmail(), is(userDtoRequest.getEmail()));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(savedUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    @DisplayName("Обновление данных пользователя, email и name равны null")
    void update_UserFoundAndNameNullEmailNull_ShouldNotUpdateAnyFields() {
        userDtoRequest.setEmail(null);
        userDtoRequest.setName(null);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(userDtoResponse);

        userService.update(userId, userDtoRequest);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(userDtoRequest.getName()));
        assertThat(savedUser.getEmail(), is(userDtoRequest.getEmail()));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(savedUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    void getUserById_UserFound_ShouldReturnDto() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userMapper.toResponse(user))
                .thenReturn(userDtoResponse);

        userService.getUserById(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    void getUserById_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.getUserById(userId));
        assertThat(e.getMessage(), is("Пользователь с ID" + userId + " не найден"));

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toResponse(user);
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void getAll_ShouldReturnList() {
        when(userRepository.findAll())
                .thenReturn(List.of(user));
        when(userMapper.toResponse(user))
                .thenReturn(userDtoResponse);

        userService.getAll();

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    @DisplayName("Удаление пользователя по id")
    void delete() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        userService.delete(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }
}
