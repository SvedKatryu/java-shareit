package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaCommentRepository;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private JpaItemRepository itemRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaBookingRepository bookingRepository;

    @Mock
    private JpaCommentRepository commentRepository;

    @Mock
    private JpaItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @Captor
    private ArgumentCaptor<OffsetPageRequest> offsetPageRequestArgumentCaptor;

    private User owner;

    private long ownerId;

    private User requester;

    private long requesterId;

    private ItemDto itemDto;

    private ItemDtoRequest itemDtoRequest;

    private Item item;

    private long itemId;

    long requestId;

    private Booking booking1;

    private Booking booking2;

    private Booking booking3;

    @BeforeEach
    void setUp() {
        ownerId = 1;
        owner = User.builder()
                .id(ownerId)
                .name("owner")
                .email("owner@email.com")
                .build();
        requesterId = 3;
        requester = User.builder()
                .id(requesterId)
                .name("requester")
                .email("requester@email.com")
                .build();
        requestId = 2;
        itemDto = ItemDto.builder()
                .name("itemDto")
                .description("itemDto description")
                .requestId(requestId)
                .available(true)
                .build();
        itemDtoRequest = ItemDtoRequest.builder()
                .name("itemDtoRequest")
                .description("itemDtoRequest description")
                .requestId(requestId)
                .available(true)
                .build();
        itemId = 4;
        item = Item.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .owner(owner)
                .available(true)
                .build();
        booking1 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .status(Status.WAITING)
                .build();
        booking2 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().minusDays(12))
                .end(LocalDateTime.now().minusDays(11))
                .status(Status.WAITING)
                .build();
        booking3 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(12))
                .status(Status.WAITING)
                .build();
    }

    @Test
    @DisplayName("Добавление вещи")
    void addItem_UserAndRequestFound_ShouldReturnItemDtoWithOwnerAndRequest() {
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        ItemRequest itemRequest = ItemRequest.builder()
                .requester(requester)
                .description("description")
                .build();
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItemWithRequest(item))
                .thenReturn(itemDto);
        itemService.addNewItem(ownerId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getOwner(), is(owner));
        assertThat(captorValue.getRequest(), is(itemRequest));
        assertThat(itemRequest.getItems(), is(List.of(captorValue)));
        verify(itemMapper, times(1)).toItemWithRequest(item);
    }

    @Test
    @DisplayName("Добавление вещи не по запросу")
    void addItem_RequestIdIsNull_ShouldReturnItemDtoWithOwnerAndWithoutRequest() {
        itemDto.setRequestId(null);
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItemWithRequest(item))
                .thenReturn(itemDto);
        itemDtoRequest.setRequestId(null);

        itemService.addNewItem(ownerId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, never()).findById(any());
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getOwner(), is(owner));
        assertThat(captorValue.getRequest(), nullValue());
        verify(itemMapper, times(1)).toItemWithRequest(item);
    }

    @Test
    @DisplayName("Добавление вещи, пользователь не найден")
    void addItem_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addNewItem(ownerId, itemDtoRequest));
        assertThat(e.getMessage(), is("Пользователь с ID" + ownerId + " не найден"));

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toItemWithRequest(any());
    }

    @Test
    @DisplayName("Добавление вещи по запросу, запрос не найден")
    void addItem_RequestNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addNewItem(ownerId, itemDtoRequest));
        assertThat(e.getMessage(), is("Запрос с ID" + requestId + " не найден"));

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, times(1)).findById(requestId);
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи")
    void updateItem_WhenAllUpdateFieldsNotNull_ShouldUpdateNameDescriptionAndAvailable() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(false)
                .build();

        item.setName("new name");
        item.setDescription("new description");
        item.setAvailable(false);

        item.setOwner(owner);
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);

        itemService.update(ownerId, itemId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemDtoRequest.getName()));
        assertThat(captorValue.getDescription(), is(itemDtoRequest.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemDtoRequest.getAvailable()));
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового названия")
    void updateItem_WhenUpdatedNameIsNull_ShouldUpdateDescriptionAndAvailable() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name(null)
                .description("new description")
                .available(false)
                .build();
        item.setDescription("new description");
        item.setAvailable(false);
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);

        itemService.update(ownerId, itemId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(item.getName()));
        assertThat(captorValue.getDescription(), is(itemDtoRequest.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemDtoRequest.getAvailable()));
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового описания")
    void updateItem_WhenUpdatedDescriptionIsNull_ShouldUpdateNameAndAvailable() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description(null)
                .available(false)
                .build();
        item.setName("new name");
        item.setAvailable(false);
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);

        itemService.update(ownerId, itemId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemDtoRequest.getName()));
        assertThat(captorValue.getDescription(), is(item.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemDtoRequest.getAvailable()));
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
    }

    @Test
    @DisplayName("Обновление данных о вещи без статуса доступности")
    void updateItem_WhenUpdatedAvailableIsNull_ShouldUpdateNameAndDescription() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setName("new name");
        item.setDescription("new description");
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(any()))
                .thenReturn(item);
        when(itemMapper.toItem(itemDtoRequest))
                .thenReturn(item);

        itemService.update(ownerId, itemId, itemDtoRequest);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemDtoRequest.getName()));
        assertThat(captorValue.getDescription(), is(itemDtoRequest.getDescription()));
        assertThat(captorValue.getAvailable(), is(item.getAvailable()));
        verify(itemMapper, times(1)).toItem(itemDtoRequest);
    }

    @Test
    @DisplayName("Обновление данных о вещи не владельцем")
    void updateItem_WhenNotOwnerTryToUpdate_ShouldThrowNotFoundException() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(requesterId, itemId, itemDtoRequest));
        assertThat(e.getMessage(), is("Пользователь не является владельцем вещи"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toItem(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи, пользователь не найден")
    void updateItem_WhenUserNotFound_ShouldThrowNotFoundException() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(requesterId, itemId, itemDtoRequest));
        assertThat(e.getMessage(), is("Пользователь с ID" + requesterId + " не найден"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toItem(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи, вещь не найдена")
    void updateItem_WhenItemNotFound_ShouldThrowNotFoundException() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(requesterId, itemId, itemDtoRequest));
        assertThat(e.getMessage(), is("Вещь с ID" + itemId + " не найдена"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, never()).save(any());
        verify(itemMapper, never()).toItem(any());
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос от владельца")
    void findItemById_WhenRequesterIsOwner_ShouldReturnItemWithBookingDates() {
        booking1.setStatus(Status.APPROVED);
        booking2.setStatus(Status.APPROVED);
        booking3.setStatus(Status.APPROVED);
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED))
                .thenReturn(List.of(booking1, booking2, booking3));
        Comment comment = new Comment();
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem();
        when(bookingMapper.toBookingDtoForItem(any()))
                .thenReturn(bookingDtoForItem);
        when(itemMapper.toResponse(eq(item)))
                .thenReturn(new ItemDtoResponse(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null, null, null));

        itemService.getItemById(ownerId, itemId);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
        verify(itemMapper, times(1)).toResponse(eq(item));
        verify(bookingMapper, times(2)).toBookingDtoForItem(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        assertThat(bookings.get(1), is(booking3));
        verify(commentMapper, times(1)).toCommentDto(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос не от владельца")
    void findItemById_WhenRequesterIsNotOwner_ShouldReturnItemWithoutBookingDates() {
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        Comment comment = new Comment();
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        ItemDtoResponse getItemDto = new ItemDtoResponse(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null, null, null);
        when(itemMapper.toResponse(item))
                .thenReturn(getItemDto);
        itemService.getItemById(requesterId, itemId);
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemMapper, times(1)).toResponse(item);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toCommentDto(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещи по id, пользователь не найден")
    void findItemById_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(requesterId, itemId));
        assertThat(e.getMessage(), is("Пользователь с ID" + requesterId + " не найден"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).findByItemIdInAndStatusNot(any(), any());
        verify(itemMapper, never()).toResponse(any());
        verify(commentRepository, never()).findByItemIdIn(any());
    }

    @Test
    @DisplayName("Поиск вещи по id, вещь не найдена")
    void findItemById_WhenItemNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(requesterId, itemId));
        assertThat(e.getMessage(), is("Вещь с ID" + itemId + " не найдена"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).findByItemIdInAndStatusNot(any(), any());
        verify(itemMapper, never()).toResponse(any());
        verify(commentRepository, never()).findByItemIdIn(any());
    }

    @Test
    @DisplayName("Поиск вещей пользователя")
    void findAllItemsByUserId_ShouldReturnItemsWithBookingAndComments() {
        long from = 0;
        int size = 4;
        booking1.setStatus(Status.APPROVED);
        booking2.setStatus(Status.APPROVED);
        booking3.setStatus(Status.APPROVED);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem();
        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .name("itemDtoRequest")
                .description("itemDtoRequest description")
                .available(true)
                .lastBooking(bookingDtoForItem)
                .nextBooking(bookingDtoForItem)
                .build();
        when(commentMapper.toCommentDto(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));

        when(bookingMapper.toBookingDtoForItem(any()))
                .thenReturn(bookingDtoForItem);
        ItemDtoResponse getItemDto = new ItemDtoResponse(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null, null, null);
        when(itemMapper.toResponse(item))
                .thenReturn(getItemDto);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toBookingDtoForItem(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        assertThat(bookings.get(1), is(booking3));
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingRepository, times(1)).findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toCommentDto(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, следующее бронирование не подтверждено")
    void findAllItemsByUserId_WhenNextBookingsIsNotApproved_ShouldReturnItemsWithBookingAndComments() {
        long from = 0;
        int size = 4;
        booking1.setStatus(Status.APPROVED);
        booking2.setStatus(Status.REJECTED);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        when(commentMapper.toCommentDto(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem();
        when(bookingMapper.toBookingDtoForItem(any()))
                .thenReturn(bookingDtoForItem);
        ItemDtoResponse getItemDto = new ItemDtoResponse(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null, null, null);
        when(itemMapper.toResponse(item))
                .thenReturn(getItemDto);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toBookingDtoForItem(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingRepository, times(1)).findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toCommentDto(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, все бронирования не подтверждены")
    void findAllItemsByUserId_WhenBookingsAreNotApproved_ShouldReturnItemsWithNoBookingAndComments() {
        long from = 1;
        int size = 4;
        booking1.setStatus(Status.WAITING);
        booking2.setStatus(Status.WAITING);
        booking3.setStatus(Status.WAITING);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        when(commentMapper.toCommentDto(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem();
        when(bookingMapper.toBookingDtoForItem(any()))
                .thenReturn(bookingDtoForItem);
        ItemDtoResponse getItemDto = new ItemDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                null);
        when(itemMapper.toResponse(item))
                .thenReturn(getItemDto);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toBookingDtoForItem(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingRepository, times(1)).findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toCommentDto(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, бронирований нет")
    void findAllItemsByUserId_WhenNoBookings_ShouldReturnItemsWithoutBookingAndComments() {
        long from = 1;
        int size = 4;
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findByItemIdIn(List.of(itemId)))
                .thenReturn(Collections.emptyList());
        ItemDtoResponse getItemDto = new ItemDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                null);
        when(itemMapper.toResponse(item))
                .thenReturn(getItemDto);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), empty());
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingRepository, times(1)).findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        verify(commentRepository, times(1)).findByItemIdIn(List.of(itemId));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, пользователь не найден")
    void findAllItemsByUserId_WhenUserNotFound_ShouldThrowNotFoundException() {
        long from = 1;
        int size = 4;
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.getItemsByUserId(requesterId, from, size));
        assertThat(e.getMessage(), is("Пользователь с ID" + requesterId + " не найден"));
        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, never()).findAllByOwnerIdOrderById(any(), any());
        verify(bookingRepository, never()).findByItemIdInAndStatusNot(any(), any());
        verify(commentRepository, never()).findByItemIdIn(any());
    }

    @Test
    @DisplayName("Поиск вещей пользователя")
    void searchItems_WhenTextIsNotBlank_ShouldReturnListOfItems() {
        long from = 1;
        int size = 4;
        String text = "search";
        when(itemRepository.searchInTitleAndDescription(any(), any()))
                .thenReturn(List.of(item));

        itemService.findItemsByText(text, from, size);

        verify(itemRepository, times(1)).searchInTitleAndDescription(stringArgumentCaptor.capture(),
                offsetPageRequestArgumentCaptor.capture());
        String captorValue = stringArgumentCaptor.getValue();
        assertThat(captorValue, is("%search%"));
        OffsetPageRequest offsetPageRequest = offsetPageRequestArgumentCaptor.getValue();
        assertThat(offsetPageRequest.getOffset(), is(from));
        assertThat(offsetPageRequest.getPageSize(), is(size));
        verify(itemMapper, times(1)).toResponse(any());
    }

    @Test
    @DisplayName("Поиск вещей, верхний регистр")
    void searchItems_WhenTextUpperCase_ShouldReturnListOfItems() {
        long from = 1;
        int size = 4;
        String text = "SEArcH";
        when(itemRepository.searchInTitleAndDescription(any(), any()))
                .thenReturn(List.of(item));

        itemService.findItemsByText(text, from, size);

        verify(itemRepository, times(1)).searchInTitleAndDescription(stringArgumentCaptor.capture(),
                offsetPageRequestArgumentCaptor.capture());
        String captorValue = stringArgumentCaptor.getValue();
        assertThat(captorValue, is("%search%"));
        OffsetPageRequest offsetPageRequest = offsetPageRequestArgumentCaptor.getValue();
        assertThat(offsetPageRequest.getOffset(), is(from));
        assertThat(offsetPageRequest.getPageSize(), is(size));
        verify(itemMapper, times(1)).toResponse(any());
    }

    @Test
    @DisplayName("Поиск вещей, пустой запрос")
    void searchItems_WhenTextIsEmpty_ShouldReturnListOfItems() {
        long from = 1;
        int size = 4;
        String text = "";

        List<ItemDtoResponse> items = itemService.findItemsByText(text, from, size);

        assertThat(items, is(Collections.emptyList()));
        verify(itemMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи")
    void addCommentToItem_WhenUserIsAbleToAddComments_ShouldReturnCommentDto() {
        CommentDto addCommentDto = CommentDto.builder().text("new comment").build();
        Comment comment = Comment.builder().text("new comment").build();
        booking1.setStatus(Status.APPROVED);
        booking2.setStatus(Status.APPROVED);
        booking3.setStatus(Status.APPROVED);
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndBookerIdOrderByStart(itemId, requesterId))
                .thenReturn(Optional.of(booking2));
        when(commentRepository.save(any()))
                .thenReturn(new Comment());
        when(commentMapper.toComment(addCommentDto))
                .thenReturn(comment);

        itemService.addComment(requesterId, itemId, addCommentDto);

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndBookerIdOrderByStart(itemId, requesterId);
        verify(commentRepository, times(1)).save(commentArgumentCaptor.capture());
        Comment captorValue = commentArgumentCaptor.getValue();
        assertThat(captorValue.getText(), is(addCommentDto.getText()));
        assertThat(captorValue.getItem(), is(item));
        assertThat(captorValue.getAuthor(), is(requester));
        assertThat(captorValue.getCreated(), lessThanOrEqualTo(LocalDateTime.now()));
        verify(commentMapper, times(1)).toComment(any());
    }


    @Test
    @DisplayName("Добавление отзыва о вещи, от пользователя не бравшего вещь в аренду")
    void addCommentToItem_WhenUserUnableToAddComments_ShouldThrowItemUnavailableException() {
        CommentDto addCommentDto = CommentDto.builder().text("new comment").build();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findFirstByItemIdAndBookerIdOrderByStart(itemId, ownerId))
                .thenThrow(new NotFoundException("Пользователь с ID1 не бронировал вещь с ID4"));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addComment(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с ID" + ownerId + " не бронировал вещь с ID" + itemId));

        verify(userRepository, times(1)).findById(ownerId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndBookerIdOrderByStart(itemId, ownerId);
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toComment(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, бронирование не закончилось")
    void addCommentToItem_WhenUserIsAbleToAddCommentsButBookingsHaveNotEnded_ShouldThrowItemUnavailableException() {
        CommentDto addCommentDto = CommentDto.builder().text("new comment").build();
        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByItemIdAndBookerIdOrderByStart(itemId, requesterId))
                .thenReturn(Optional.of(booking1));

        ValidationException e = assertThrows(ValidationException.class,
                () -> itemService.addComment(requesterId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь может оставлять отзыв только после окончания срока аренды!"));

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1)).findFirstByItemIdAndBookerIdOrderByStart(itemId, requesterId);
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toComment(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, пользователь не найден")
    void addCommentToItem_WhenUserNotFound_ShouldThrowItemUnavailableException() {
        CommentDto addCommentDto = CommentDto.builder().text("new comment").build();
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addComment(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с ID" + ownerId + " не найден"));

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).findFirstByItemIdAndBookerIdOrderByStart(any(), any());
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toComment(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, вещь не найдена")
    void addCommentToItem_WhenItemNotFound_ShouldThrowItemUnavailableException() {
        CommentDto addCommentDto = CommentDto.builder().text("new comment").build();
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addComment(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Вещь с ID" + itemId + " не найдена"));

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).findFirstByItemIdAndBookerIdOrderByStart(any(), any());
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toComment(any());
    }
}
