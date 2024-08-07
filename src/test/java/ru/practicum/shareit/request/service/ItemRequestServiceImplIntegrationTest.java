package ru.practicum.shareit.request.service;


import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private JpaUserRepository userRepository;

    private User savedUser;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    private ItemRequestDtoRequest addItemRequestDto;
    ItemRequestDtoRequest addItemRequestDto2;
    ItemRequestDtoRequest addItemRequestDto3;

    private ItemRequest itemRequest;

    @BeforeEach
    public void setUp() {
        User user = User.builder().name("username").email("test@email.com").build();
        savedUser = userRepository.save(user);
        addItemRequestDto = ItemRequestDtoRequest.builder().description("description").build();
        addItemRequestDto2 = ItemRequestDtoRequest.builder().description("description 2").build();
        addItemRequestDto3 = ItemRequestDtoRequest.builder().description("description 3").build();
        itemRequest = ItemRequest.builder().id(1L).description("description").build();
    }

    @AfterAll
    public void deleteUsers() {
        userRepository.deleteAll();
    }

    @AfterAll
    void deleteUsersAfterTest() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление нового запроса")
    void addNewItemRequest_ShouldReturnRequestDto() {
        when(itemRequestMapper.toItemRequest(addItemRequestDto))
                .thenReturn(itemRequest);
        ItemRequestDtoResponse itemRequestDto = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        assertThat(itemRequestDto, notNullValue());
        assertThat(itemRequestDto.getId(), greaterThan(0L));
        assertThat(itemRequestDto.getDescription(), is(addItemRequestDto.getDescription()));
        assertThat(itemRequestDto.getCreated(), notNullValue());
    }

    @Test
    @DisplayName("Добавление нового запроса, пользователь не найден")
    void addNewItemRequest_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.addNewItemRequest(999L, addItemRequestDto));

        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя")
    void getAllItemRequestsFromUser_ShouldReturnRequestList() {
        ItemRequestDtoResponse itemRequestDto = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        List<ItemRequestDtoResponse> requests = itemRequestService.getAllItemRequestsFromUser(savedUser.getId());

        assertThat(requests, notNullValue());
        assertThat(requests, is(List.of(itemRequestDto)));
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя, пользователь не найден")
    void getAllItemRequestsFromUser_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsFromUser(999L));

        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Поиск доступных запросов")
    @SneakyThrows
    void getAvailableItemRequests_From0Size5_ShouldReturn2Requests() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        User savedUser2 = userRepository.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDtoResponse savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        Thread.sleep(100L);
        ItemRequestDtoResponse savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDtoResponse> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 0L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, is(List.of(savedRequest3, savedRequest2)));
    }

    @Test
    @DisplayName("Поиск доступных запросов со 2го элемента")
    @SneakyThrows
    void getAvailableItemRequests_From1Size5_ShouldReturn1Requests() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        User savedUser2 = userRepository.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDtoResponse savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        Thread.sleep(100L);
        ItemRequestDtoResponse savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDtoResponse> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 1L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, is(List.of(savedRequest2)));
    }

    @Test
    @DisplayName("Поиск доступных запросов с 3го элемента")
    void getAvailableItemRequests_From2Size5_ShouldReturnEmptyList() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        User savedUser2 = userRepository.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDtoResponse savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        ItemRequestDtoResponse savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDtoResponse> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 2L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, emptyIterable());
    }

    @Test
    @DisplayName("Поиск запроса по id")
    void getItemRequestById_ShouldReturnItem() {
        ItemRequestDtoResponse savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        ItemRequestDtoResponse request = itemRequestService.getItemRequestById(savedUser.getId(), savedRequest.getId());

        assertThat(request, notNullValue());
        assertThat(request, is(savedRequest));
    }

    @Test
    @DisplayName("Поиск запроса по id, пользователь не найден")
    void getItemRequestById_UserNotFound_ShouldThrowNotFoundException() {
        ItemRequestDtoResponse savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(999L, savedRequest.getId()));

        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Поиск запроса по id, запрос не найден")
    void getItemRequestById_RequestNotFound_ShouldThrowNotFoundException() {
        ItemRequestDtoResponse savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(savedUser.getId(), 999L));

        assertThat(e.getMessage(), is("Запрос с ID999 не найден"));
    }
}
