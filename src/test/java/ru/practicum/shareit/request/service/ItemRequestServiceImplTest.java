package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.dto.ItemDto;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private JpaItemRequestRepository itemRequestRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;

    private ItemRequestDtoResponse itemResponseDto;

    private long userId;

    private ItemRequest itemRequest;

    ItemDto item;

    @BeforeEach
    public void setUp() {
        userId = 1;
        itemRequest = ItemRequest.builder().id(1L).description("description").build();
        item = ItemDto.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        itemResponseDto = ItemRequestDtoResponse.builder().items(List.of(item)).build();
    }

    @Test
    @DisplayName("Добавление нового запроса")
    public void addNewItemRequest_ShouldSetRequester() {
        User user = new User();
        String description = "description";
        ItemRequestDtoRequest addItemRequestDto = new ItemRequestDtoRequest(description);
        ItemRequest itemRequest2 = ItemRequest.builder().description(description).build();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(addItemRequestDto))
                .thenReturn(itemRequest);
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest2);

        itemRequestService.addNewItemRequest(userId, addItemRequestDto);

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestMapper, times(1)).toItemRequest(addItemRequestDto);
        verify(itemRequestRepository, times(1)).save(itemRequestArgumentCaptor.capture());

        ItemRequest captorValue = itemRequestArgumentCaptor.getValue();

        assertThat(captorValue, is(notNullValue()));
        assertThat(captorValue.getRequester(), is(user));
    }

    @Test
    @DisplayName("Добавление нового запроса, пользователь не найден")
    public void addNewItemRequest_NoUserFound_ThrowNotFoundException() {
        ItemRequestDtoRequest addItemRequestDto = new ItemRequestDtoRequest();
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.addNewItemRequest(userId, addItemRequestDto));
        assertThat(e.getMessage(), is("Пользователь с ID1 не найден"));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestMapper, never()).toItemRequest(any());
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя, пустой список")
    public void getAllItemRequestsFromUser_ShouldReturnEmptyList() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findRequestsFromUser(userId))
                .thenReturn(Collections.emptyList());
        when(itemRequestMapper.toDtoResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDtoResponse> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(Collections.emptyList()));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findRequestsFromUser(userId);
        verify(itemRequestMapper, times(1)).toDtoResponseList(Collections.emptyList());
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя")
    public void getAllItemRequestsFromUser_ShouldReturnRequestList() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findRequestsFromUser(userId))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toDtoResponseList(List.of(itemRequest)))
                .thenReturn(List.of(itemResponseDto));


        List<ItemRequestDtoResponse> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(List.of(itemResponseDto)));
        assertThat(requests.size(), is(1));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findRequestsFromUser(userId);
        verify(itemRequestMapper, times(1)).toDtoResponseList(List.of(itemRequest));
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя, пользователь не найден")
    public void getAllItemRequestsFromUser_NoUserFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsFromUser(userId));

        assertThat(e.getMessage(), is("Пользователь с ID1 не найден"));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, never()).findRequestsFromUser(userId);
        verify(itemRequestMapper, never()).toDtoResponseList(any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, пользователь не найден")
    public void getAvailableItemRequests_UserNotExists_ShouldThrowNotFoundException() {
        Long from = 1L;
        Integer size = 1;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Пользователь с ID1 не найден"));

        verify(itemRequestRepository, never()).findAllRequests();
        verify(itemRequestRepository, never()).findAvailableRequests(userId, OffsetPageRequest.of(from, size));
        verify(itemRequestMapper, never()).toDtoResponseList(any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, from = null")
    public void getAvailableItemRequests_FromNullSizeNotNull_ShouldThrowIllegalArgumentException() {
        Long from = null;
        Integer size = 1;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Offset must be positive or zero!"));

        verify(itemRequestRepository, never()).findAllRequests();
        verify(itemRequestMapper, never()).toDtoResponseList(any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, size = null")
    public void getAvailableItemRequests_FromNotNullSizeNull_ShouldThrowIllegalArgumentException() {
        Long from = 1L;
        Integer size = null;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Page size must be positive!"));

        verify(itemRequestRepository, never()).findAllRequests();
        verify(itemRequestMapper, never()).toDtoResponseList(any());
    }

    @Test
    @DisplayName("Поиск доступных запросов")
    public void getAvailableItemRequests_WithNotNullFromAndSize_ShouldInvokeFindAllPageable() {
        Long from = 1L;
        Integer size = 2;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findAvailableRequests(eq(userId), any()))
                .thenReturn(Page.empty());

        itemRequestService.getAvailableItemRequests(userId, from, size);

        verify(itemRequestRepository, never()).findAllRequests();
        verify(itemRequestRepository, times(1)).findAvailableRequests(eq(userId),
                any());
        verify(itemRequestMapper, times(1)).toDtoResponseList(any());
    }

    @Test
    @DisplayName("Поиск запроса по id, пользователь не найден")
    public void getItemRequestById_UserNotFound_ShouldThrowNotFoundException() {
        long requestId = 1;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));

        assertThat(e.getMessage(), is("Пользователь с ID1 не найден"));

        verify(itemRequestMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Поиск запроса по id, запрос не найден")
    public void getItemRequestById_RequestNotFound_ShouldThrowNotFoundException() {
        long requestId = 1;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));

        assertThat(e.getMessage(), is("Запрос с ID1 не найден"));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Поиск запроса по id")
    public void getItemRequestById_ShouldReturnRequest() {
        long requestId = 1;
        itemRequest.setId(1L);


        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toResponse(itemRequest))
                .thenReturn(itemResponseDto);

        itemRequestService.getItemRequestById(userId, requestId);

        verify(userRepository, times(1)).findById(userId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemRequestMapper, times(1)).toResponse(itemRequest);
    }
}
