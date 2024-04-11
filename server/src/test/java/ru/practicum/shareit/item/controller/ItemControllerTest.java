package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemServiceImpl itemService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private ItemMapper itemMapper;

    private long userId;

    private String header;

    private Item item;

    private ItemDto itemDto;
    private ItemDtoRequest itemDtoRequest;
    private ItemDtoResponse itemDtoResponse;
    private CommentDto commentDto;

    private Long itemId;

    ItemDtoRequest request;

    @BeforeEach
    void setUp() {
        userId = 1L;
        header = "X-Sharer-User-Id";
        itemDto = ItemDto.builder()
                .name("name")
                .available(true)
                .description("description")
                .build();
        itemDtoRequest = ItemDtoRequest.builder()
                .name("name")
                .available(true)
                .description("description")
                .build();
        itemDtoResponse = ItemDtoResponse.builder()
                .name("name")
                .available(true)
                .description("description")
                .build();
        commentDto = CommentDto.builder()
                .text("name")
                .authorName("authorName")
                .build();
        itemId = 2L;
    }

    @Test
    @DisplayName("Добавление вещи")
    @SneakyThrows
    void addItem_ShouldReturnStatus200() {
        when(itemService.addNewItem(userId, itemDtoRequest))
                .thenReturn(itemDto);
        when(itemMapper.toItemWithRequest(item)).thenReturn(itemDto);
        when(itemMapper.toItem(itemDtoRequest)).thenReturn(item);

        mvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(itemDto)))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).addNewItem(userId, itemDtoRequest);
    }

    @Test
    @DisplayName("Добавление вещи, запрос без заголовка")
    @SneakyThrows
    void addItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        when(itemService.addNewItem(userId, itemDtoRequest))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).addNewItem(any(), any());
    }

    @Test
    @DisplayName("Обновление данных о вещи")
    @SneakyThrows
    void updateItem_ShouldReturnStatus200() {
        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("updated name")
                .description("updated description")
                .available(false)
                .build();
        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .name("updated name")
                .description("updated description")
                .available(false)
                .build();
        when(itemMapper.toItem(itemUpdateDto)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemDtoResponse);
        when(itemService.update(userId, itemId, itemUpdateDto))
                .thenReturn(itemDtoResponse);


        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(itemDtoResponse)))
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable())));

        verify(itemService, times(1)).update(userId, itemId, itemUpdateDto);
    }

    @Test
    @DisplayName("Обновление данных о вещи, запрос без заголовка")
    @SneakyThrows
    void updateItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("updated name")
                .description("updated description")
                .available(false)
                .build();
        when(itemService.update(userId, itemId, itemUpdateDto))
                .thenReturn(itemDtoResponse);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("Получение вещи по id")
    @SneakyThrows
    void getItemById_ShouldReturnStatus200() {
        when(itemService.getItemById(userId, itemId))
                .thenReturn(itemDtoResponse);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(itemDtoResponse)))
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(itemDtoResponse.getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemDtoResponse.getNextBooking())))
                .andExpect(jsonPath("$.comments", is(itemDtoResponse.getComments())));

        verify(itemService, times(1)).getItemById(userId, itemId);
    }

    @Test
    @DisplayName("Получение вещи по id, запрос без заголовка")
    @SneakyThrows
    void getItemById_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        when(itemService.getItemById(userId, itemId))
                .thenReturn(itemDtoResponse);

        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).getItemById(any(), any());
    }

    @Test
    @DisplayName("Получение вещей пользователя с параметрами по умолчанию")
    @SneakyThrows
    void getAllItemsByUserId_WithoutParams_ShouldReturnStatus200() {
        long from = 0L;
        int size = 10;
        when(itemService.getItemsByUserId(userId, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(List.of(itemDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDtoResponse.getAvailable())))
                .andExpect(jsonPath("$.[0].lastBooking", is(itemDtoResponse.getLastBooking())))
                .andExpect(jsonPath("$.[0].nextBooking", is(itemDtoResponse.getNextBooking())))
                .andExpect(jsonPath("$.[0].comments", is(itemDtoResponse.getComments())));

        verify(itemService, times(1)).getItemsByUserId(userId, from, size);
    }

    @Test
    @DisplayName("Получение вещей пользователя")
    @SneakyThrows
    void getAllItemsByUserId_WithParams_ShouldReturnStatus200() {
        long from = 1;
        int size = 5;
        when(itemService.getItemsByUserId(userId, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(List.of(itemDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDtoResponse.getAvailable())))
                .andExpect(jsonPath("$.[0].lastBooking", is(itemDtoResponse.getLastBooking())))
                .andExpect(jsonPath("$.[0].nextBooking", is(itemDtoResponse.getNextBooking())))
                .andExpect(jsonPath("$.[0].comments", is(itemDtoResponse.getComments())));

        verify(itemService, times(1)).getItemsByUserId(userId, from, size);
    }

    @Test
    @DisplayName("Получение вещей пользователя")
    @SneakyThrows
    void getAllItemsByUserId_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        long from = 1;
        int size = 4;
        when(itemService.getItemsByUserId(userId, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).getItemsByUserId(any(), eq(from), eq(size));
    }

    @Test
    @DisplayName("Поиск вещей, запрос без параметров")
    @SneakyThrows
    void searchItems_WithoutParams_ShouldReturnStatus200() {
        String text = "search";
        long from = 0;
        int size = 10;
        when(itemService.findItemsByText(text, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(List.of(itemDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDtoResponse.getAvailable())));

        verify(itemService, times(1)).findItemsByText(text, from, size);
    }

    @Test
    @DisplayName("Поиск вещей")
    @SneakyThrows
    void searchItems_WithParams_ShouldReturnStatus200() {
        String text = "search";
        long from = 1;
        int size = 5;
        when(itemService.findItemsByText(text, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(List.of(itemDtoResponse))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDtoResponse.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(itemDtoResponse.getAvailable())));

        verify(itemService, times(1)).findItemsByText(text, from, size);
    }

    @Test
    @DisplayName("Поиск вещей, пустой запрос")
    @SneakyThrows
    void searchItems_WithoutText_ShouldThrowMissingServletRequestParameterExceptionExceptionAndStatus400() {
        long from = 0;
        int size = 10;
        String text = "";
        when(itemService.findItemsByText(text, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(Collections.emptyList())));

        verify(itemService, never()).findItemsByText(any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, запрос без заголовка")
    @SneakyThrows
    void searchItems_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        long from = 0;
        int size = 10;
        String text = "search";
        when(itemService.findItemsByText(text, from, size))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).findItemsByText(any(), any(), any());
    }

    @Test
    @DisplayName("Добавление комментария")
    @SneakyThrows
    void addCommentToItem_ShouldReturnStatus201() {
        when(itemService.addComment(userId, itemId, commentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(mapper.writeValueAsString(commentDto)))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated())));

        verify(itemService, times(1)).addComment(userId, itemId, commentDto);
    }

    @Test
    @DisplayName("Добавление комментария, запрос без заголовка")
    @SneakyThrows
    void addCommentToItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndReturnStatus400() {
        when(itemService.addComment(userId, itemId, commentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).addComment(any(), any(), any());
    }
}
