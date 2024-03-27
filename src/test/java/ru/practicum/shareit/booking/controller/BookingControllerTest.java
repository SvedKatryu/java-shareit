package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingServiceImpl bookingService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long userId;

    private String header;
    private User user;

    private long bookingId;

    private long itemId;

    private BookingDtoRequest bookingDtoRequest;

    private BookingDtoResponse bookingDtoResponse;

    private ItemDtoResponse itemDtoResponse;
    private UserDtoResponse userDtoResponse;
    private JpaUserRepository userRepository;
    public static final LocalDateTime START =
            LocalDateTime.of(2030, 5, 22, 16, 41, 8);
    public static final LocalDateTime END = START.plusDays(14);

    @BeforeEach
    void setUp() {
        userId = 1L;
        bookingId = 2L;
        itemId = 3L;
        header = "X-Sharer-User-Id";
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(START)
                .end(END)
                .build();
        bookingDtoResponse = BookingDtoResponse.builder()
                .id(bookingId)
                .start(START)
                .end(END)
                .item(itemDtoResponse)
                .booker(userDtoResponse)
                .status(Status.WAITING)
                .build();
        itemDtoResponse = ItemDtoResponse.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(true)
                .build();
        userDtoResponse = UserDtoResponse.builder()
                .name("name")
                .email("test@mail.com")
                .build();
    }

    @Test
    @DisplayName("Добавление бронирования")
    @SneakyThrows
    void add_ShouldReturnStatus201() {
        when(bookingService.add(userId, bookingDtoRequest))
                .thenReturn(bookingDtoResponse);

        mvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(bookingDtoResponse)))
                .andExpect(jsonPath("$.id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoResponse.getEnd().toString())));


        verify(bookingService, times(1)).add(userId, bookingDtoRequest);
    }

    @Test
    @DisplayName("Добавление бронирования, дата старта в прошлом")
    @SneakyThrows
    void add_BookingStartInPast_ShouldThrowMethodArgumentNotValidException() {
        bookingDtoRequest.setStart(LocalDateTime.now().minusDays(1));

        mvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
        verify(bookingService, never()).add(any(), any());
    }

    @Test
    @DisplayName("Добавление бронирования, запрос без заголовка")
    @SneakyThrows
    void add_WithoutHeader_ShouldThrowMissingRequestHeaderException() {

        mvc.perform(post("/bookings")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingService, never()).add(any(), any());
    }

    @Test
    @DisplayName("Добавление бронирования, вещь не найдена")
    @SneakyThrows
    void add_WhenNotFoundThrown_ShouldReturn404Status() {
        when(bookingService.add(userId, bookingDtoRequest))
                .thenThrow(NotFoundException.class);

        mvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

        verify(bookingService, times(1)).add(userId, bookingDtoRequest);
    }

    @Test
    @DisplayName("Добавление бронирования собственной вещи")
    @SneakyThrows
    void add_WhenNotAuthorizedThrown_ShouldReturn404() {
        String errorMessage = "Владелец вещи не может забронировать свою вещь.";
        when(bookingService.add(userId, bookingDtoRequest))
                .thenThrow(new NotFoundException(errorMessage));

        mvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.error", is(errorMessage)));
        verify(bookingService, times(1)).add(userId, bookingDtoRequest);
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    @SneakyThrows
    void approve_WithAllParams_ShouldReturnStatus200() {
        Long bookingId = 2L;
        Boolean approved = true;
        when(bookingService.approve(userId, bookingId, approved))
                .thenReturn(bookingDtoResponse);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(bookingDtoResponse)))
                .andExpect(jsonPath("$.id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).approve(userId, bookingId, approved);
    }

    @Test
    @DisplayName("Подтверждение бронирования недоступной вещи")
    @SneakyThrows
    void approve_WhenItemUnavailableExceptionThrown_ShouldReturn404() {
        Long bookingId = 2L;
        Boolean approved = true;
        String errorMessage = "Статус бронирования уже 'APPROVED'";
        when(bookingService.approve(userId, bookingId, approved))
                .thenThrow(new ValidationException(errorMessage));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ValidationException))
                .andExpect(jsonPath("$.error", is(errorMessage)));

        verify(bookingService, times(1)).approve(userId, bookingId, approved);
    }

    @Test
    @DisplayName("Подтверждение бронирования, выброшено непредвиденное исключение")
    @SneakyThrows
    void approve_WhenUnexpectedException_ShouldReturn500() {
        Long bookingId = 2L;
        Boolean approved = true;
        when(bookingService.approve(userId, bookingId, approved))
                .thenThrow(RuntimeException.class);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));

        verify(bookingService, times(1)).approve(userId, bookingId, approved);
    }

    @Test
    @DisplayName("Подтверждение бронирования, запрос без заголовка")
    @SneakyThrows
    void approve_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        Long bookingId = 2L;
        Boolean approved = true;
        when(bookingService.approve(userId, bookingId, approved))
                .thenReturn(bookingDtoResponse);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
    }

    @Test
    @DisplayName("Подтверждение бронирования, запрос без статуса подтверждения")
    @SneakyThrows
    void approve_WithoutApproved_ShouldThrowMissingServletRequestParameterException() {
        Long bookingId = 2L;
        Boolean approved = true;
        when(bookingService.approve(userId, bookingId, approved))
                .thenReturn(bookingDtoResponse);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingServletRequestParameterException));

    }

    @Test
    @DisplayName("Поиск бронирования по id")
    @SneakyThrows
    void get_WithAllParameters_ShouldReturnStatus200() {
        Long bookingId = 2L;
        when(bookingService.get(userId, bookingId))
                .thenReturn(bookingDtoResponse);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(bookingDtoResponse)))
                .andExpect(jsonPath("$.id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).get(userId, bookingId);
    }

    @Test
    @DisplayName("Поиск бронирования по id, запрос без заголовка")
    @SneakyThrows
    void get_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        Long bookingId = 2L;

        mvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingService, never()).get(any(), any());
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя")
    @SneakyThrows
    void get_WithAllParams_ShouldReturnStatus200() {
        State state = State.FUTURE;
        Long from = 1L;
        Integer size = 5;
        boolean isOwner = false;
        when(bookingService.getAllByBooker(userId, state, from, size))
                .thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(bookingDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.[0].booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.[0].status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.[0].start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).getAllByBooker(userId, state, from, size);
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя с параметрами по умолчанию")
    @SneakyThrows
    void getAllByBooker_WithoutParams_ShouldReturnStatus200() {
        long from = 0;
        int size = 10;
        State state = State.ALL;
        boolean isOwner = false;
        when(bookingService.getAllByBooker(userId, state, from, size))
                .thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get("/bookings")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(bookingDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.[0].booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.[0].status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.[0].start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).getAllByBooker(userId, state, from, size);
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя с параметрами")
    @SneakyThrows
    void getAllByBooker_WithParams_ShouldReturnStatus200() {
        long from = 2;
        int size = 5;
        State state = State.ALL;
        boolean isOwner = false;
        when(bookingService.getAllByBooker(userId, state, from, size))
                .thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(bookingDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.[0].booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.[0].status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.[0].start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).getAllByBooker(userId, state, from, size);
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя, запрос без заголовка")
    @SneakyThrows
    void getAllByBooker_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        State state = State.FUTURE;
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings")
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingService, never()).getAllByBooker(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Поиск всех бронирований вещей пользователя")
    @SneakyThrows
    void getAllByOwner_WithAllParams_ShouldReturnStatus200() {
        State state = State.FUTURE;
        Long from = 1L;
        Integer size = 5;
        when(bookingService.getAllByOwner(userId, state, from, size))
                .thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(bookingDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.[0].booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.[0].status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.[0].start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).getAllByOwner(userId, state, from, size);
    }

    @Test
    @DisplayName("Поиск всех бронирований вещей пользователя с параметрами по умолчанию")
    @SneakyThrows
    void getAllByOwner_WithoutParams_ShouldReturnStatus200() {
        Long from = 0L;
        Integer size = 10;
        State state = State.ALL;
        when(bookingService.getAllByOwner(userId, state, from, size))
                .thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get("/bookings/owner")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(bookingDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(bookingDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item", is(bookingDtoResponse.getItem())))
                .andExpect(jsonPath("$.[0].booker", is(bookingDtoResponse.getBooker())))
                .andExpect(jsonPath("$.[0].status", is(bookingDtoResponse.getStatus().toString())))
                .andExpect(jsonPath("$.[0].start", is(bookingDtoResponse.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDtoResponse.getEnd().toString())));

        verify(bookingService, times(1)).getAllByOwner(userId, state, from, size);
    }

    @Test
    @DisplayName("Поиск всех бронирований вещей пользователя, запрос без заголовка")
    @SneakyThrows
    void getAllByOwner_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        State state = State.FUTURE;
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings/owner")
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingService, never()).getAllByOwner(any(), any(), any(), any());
    }
}