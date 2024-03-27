package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.model.State.ALL;
import static ru.practicum.shareit.booking.model.State.CURRENT;
import static ru.practicum.shareit.booking.model.State.FUTURE;
import static ru.practicum.shareit.booking.model.State.PAST;
import static ru.practicum.shareit.booking.model.State.REJECTED;
import static ru.practicum.shareit.booking.model.State.WAITING;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private JpaBookingRepository bookingRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @Captor
    private ArgumentCaptor<OffsetPageRequest> offsetPageRequestArgumentCaptor;

    private long userId;

    private long bookingId;

    private long itemId;

    private Item item;
    private ItemDtoResponse itemDtoResponse;
    private BookingDtoRequest bookingDtoRequest;

    private BookingDtoResponse bookingDtoResponse;

    private User itemOwner;

    private User booker;

    private Booking booking;

    @BeforeEach
    void setUp() {
        userId = 1L;
        bookingId = 2L;
        itemId = 3L;
        itemOwner = User.builder()
                .id(5L)
                .name("name")
                .email("email@mail.com")
                .build();
        item = Item.builder()
                .id(itemId)
                .name("name")
                .description("description")
                .available(true)
                .owner(itemOwner)
                .build();
        itemDtoResponse = ItemDtoResponse.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(true)
                .build();
        booker = User.builder()
                .id(userId)
                .name("owner")
                .email("owner@email.com")
                .build();
        booking = Booking.builder()
                .id(bookingId)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .item(item)
                .booker(booker)
                .build();
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        bookingDtoResponse = BookingDtoResponse.builder()
                .id(bookingId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .item(itemDtoResponse)
                .status(Status.WAITING)
                .build();
    }

    @Test
    @DisplayName("Добавление бронирования")
    void addBooking_ItemAndUserFound_ShouldReturnBookingDto() {

        User user = new User();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(any(), any(), any(), any()))
                .thenReturn(booking);
        when(bookingMapper.toBookingDtoResponse(any()))
                .thenReturn(bookingDtoResponse);
        bookingService.add(userId, bookingDtoRequest);

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());
        Booking captorValue = bookingArgumentCaptor.getValue();

        assertThat(captorValue.getItem(), is(item));
        assertThat(captorValue.getBooker(), is(booker));
        assertThat(captorValue.getStatus(), is(Status.WAITING));
        assertThat(captorValue.getStart(), lessThanOrEqualTo(bookingDtoRequest.getStart()));
        assertThat(captorValue.getEnd(), lessThanOrEqualTo(bookingDtoRequest.getEnd()));

    }

    @Test
    @DisplayName("Добавление бронирования, пользователь не найден")
    void addBooking_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.add(userId, bookingDtoRequest));

        assertThat(e.getMessage(), is("Пользователь с ID " + userId + " не найден."));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Добавление бронирования, вещь не найдена")
    void addBooking_ItemNotFound_ShouldThrowNotFoundException() {
        User user = new User();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));


        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.add(userId, bookingDtoRequest));

        assertThat(e.getMessage(), is("Вещь с ID" + itemId + " не найдена."));


        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Подтверждение бронирования не владельцем вещи")
    void addBooking_OwnerTryToBookHisItem_ShouldThrowNotAuthorizedException() {
        User user = new User();
        itemOwner.setId(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.add(userId, bookingDtoRequest));

        assertThat(e.getMessage(), is("Владелец вещи не может забронировать свою вещь."));

        verify(userRepository, times(1)).findById(userId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void approve_UserAndBookingFoundAndSApprovedTrue_ShouldReturnBookingDto() {
        itemOwner.setId(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking))
                .thenReturn((booking));

        bookingService.approve(userId, bookingId, true);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, times(1)).toBookingDtoResponse(bookingArgumentCaptor.capture());

        Booking captorValue = bookingArgumentCaptor.getValue();
        assertThat(captorValue.getStatus(), is(Status.APPROVED));
    }

    @Test
    @DisplayName("Отмена бронирования")
    void approve_UserAndBookingFoundAndApprovedFalse_ShouldReturnBookingDto() {
        itemOwner.setId(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking))
                .thenReturn((booking));

        bookingService.approve(userId, bookingId, false);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, times(1)).toBookingDtoResponse(bookingArgumentCaptor.capture());
        Booking captorValue = bookingArgumentCaptor.getValue();

        assertThat(captorValue.getStatus(), is(Status.REJECTED));
    }

    @Test
    @DisplayName("Подтверждение бронирования с неверным статусом")
    void approve_UserAndBookingFoundBookingStatusNotWaiting_ShouldThrowItemUnavailableException() {
        itemOwner.setId(userId);
        booking.setStatus(Status.APPROVED);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        ValidationException e = assertThrows(ValidationException.class,
                () -> bookingService.approve(userId, bookingId, false));

        assertThat(e.getMessage(), is("Статус бронирования уже 'APPROVED'"));

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Подтверждение бронирования, пользователь не найден")
    void approve_UserNotFound_ShouldThrowNotFoundException() {
        itemOwner.setId(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approve(userId, bookingId, false));

        assertThat(e.getMessage(), is("Пользователь с ID " + userId + " не найден."));

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, never()).findById(any());
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Подтверждение бронирования, бронирование не найдено")
    void approve_BookingNotFound_ShouldThrowNotFoundException() {
        itemOwner.setId(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approve(userId, bookingId, false));

        assertThat(e.getMessage(), is("Бронь с ID " + bookingId + " не найдена."));

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос от бронирующего")
    void get_RequesterIsBooker() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.get(userId, bookingId);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, times(1)).toBookingDtoResponse(booking);
    }

    @Test
    @DisplayName("Получение бронирования по id, неавторизованный запрос")
    void get_UnauthorizedRequest_ShouldThrowNotAuthorizedException() {
        long unknownUserId = 99L;
        when(userRepository.findById(unknownUserId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.get(unknownUserId, bookingId));
        assertThat(e.getMessage(), is("Доступ к бронированию имеет только владелец или автор бронирования!"));

        verify(userRepository, times(1)).findById(unknownUserId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Получение бронирования по id, пользователь не найден")
    void get_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.get(userId, bookingId));
        assertThat(e.getMessage(), is("Пользователь с ID " + userId + " не найден."));

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, never()).findById(any());
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Получение бронирования по id, бронирование не найдено")
    void get_BookingNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.get(userId, bookingId));
        assertThat(e.getMessage(), is("Бронь с ID " + bookingId + " не найдена."));
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingMapper, never()).toBookingDtoResponse(any());
    }

    @Test
    @DisplayName("Получение всех бронирований от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStateAll_ShouldReturnListOfBookings() {
        State state = ALL;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(eq(userId), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdOrderByStartDesc(eq(userId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение текущих бронирований от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStateCurrent_ShouldReturnListOfBookings() {
        State state = CURRENT;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(userId), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(userId), any(), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение прошедших бронирований от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStatePast_ShouldReturnListOfBookings() {
        State state = PAST;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение будущих бронирований от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStateFuture_ShouldReturnListOfBookings() {
        State state = FUTURE;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStateWaiting_ShouldReturnListOfBookings() {
        State state = WAITING;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdAndStatusIsOrderByStartDesc(eq(userId), eq(Status.WAITING), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusIsOrderByStartDesc(eq(userId),
                eq(Status.WAITING), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED от владельца, начиная с 1го элемента по 2 на странице")
    void getAllByOwner_RequesterIsOwnerFromAndSizeAreNotNullStateRejected_ShouldReturnListOfBookings() {
        State state = REJECTED;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByItemOwnerIdAndStatusIsOrderByStartDesc(eq(userId), eq(Status.REJECTED), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByOwner(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusIsOrderByStartDesc(eq(userId),
                eq(Status.REJECTED), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение всех бронирований, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStateAll_ShouldReturnListOfBookings() {
        State state = ALL;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(eq(userId), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(eq(userId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение текущих бронирований, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStateCurrent_ShouldReturnListOfBookings() {
        State state = CURRENT;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(userId), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(userId), any(), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение прошедших бронирований, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStatePast_ShouldReturnListOfBookings() {
        State state = PAST;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndIsBeforeOrderByStartDesc(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение будущих бронирований, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStateFuture_ShouldReturnListOfBookings() {
        State state = FUTURE;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartIsAfterOrderByStartDesc(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStateWaiting_ShouldReturnListOfBookings() {
        State state = WAITING;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(eq(userId), eq(Status.WAITING), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusIsOrderByStartDesc(eq(userId),
                eq(Status.WAITING), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED, начиная с 1го элемента по 2 на странице")
    void getAllByBooker_RequesterIsNotOwnerFromAndSizeAreNotNullStateRejected_ShouldReturnListOfBookings() {
        State state = REJECTED;
        Long from = 1L;
        Integer size = 2;
        when(userRepository.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(eq(userId), eq(Status.REJECTED), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllByBooker(userId, state, from, size);

        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusIsOrderByStartDesc(eq(userId),
                eq(Status.REJECTED), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toBookingDtoResponseList(List.of(booking));
    }
}
