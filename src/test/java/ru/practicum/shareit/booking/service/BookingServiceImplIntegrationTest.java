package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JpaItemRepository itemRepository;

    @Autowired
    private JpaUserRepository userRepository;

    private User owner;

    private User booker;

    private Item savedItem1;

    private Item savedItem2;

    private BookingDtoRequest addBookingDto1;

    private BookingDtoRequest addBookingDto2;

    private BookingDtoRequest addBookingDto3;

    @BeforeAll
    void setUp() {
        User user1 = User.builder()
                .name("owner")
                .email("owner@mail.com")
                .build();
        owner = userRepository.save(user1);

        User user2 = User.builder()
                .name("booker")
                .email("booker@mail.com")
                .build();
        booker = userRepository.save(user2);

        Item item1 = Item.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .build();
        savedItem1 = itemRepository.save(item1);

        Item item2 = Item.builder()
                .name("itemName2")
                .description("itemDescription2")
                .available(true)
                .owner(booker)
                .build();
        savedItem2 = itemRepository.save(item2);

        addBookingDto1 = BookingDtoRequest.builder()
                .itemId(savedItem1.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        addBookingDto2 = BookingDtoRequest.builder()
                .itemId(savedItem1.getId())
                .start(LocalDateTime.now().plusDays(7))
                .end(LocalDateTime.now().plusDays(8))
                .build();

        addBookingDto3 = BookingDtoRequest.builder()
                .itemId(savedItem2.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
    }

    @AfterAll
    public void cleanDb() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление бронирования")
    void add_ShouldReturnBookingDtoWithIdNotNull() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        assertThat(addedBooking.getId(), notNullValue());
        assertThat(addedBooking.getItem().getId(), is(savedItem1.getId()));
        assertThat(addedBooking.getStart(), notNullValue());
        assertThat(addedBooking.getEnd(), notNullValue());
        assertThat(addedBooking.getStatus(), is(Status.WAITING));
    }

    @Test
    @DisplayName("Добавление бронирования на собственную вещь")
    void add_WhenOwnerTryToBookHisOwnItem_ShouldThrowNotAuthorizedException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.add(owner.getId(), addBookingDto1));
        assertThat(e.getMessage(), is("Владелец вещи не может забронировать свою вещь."));
    }

    @Test
    @DisplayName("Добавление бронирования, пользователь не найден")
    void add_WhenUserNotExists_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.add(999L, addBookingDto1));
        assertThat(e.getMessage(), is("Пользователь с ID 999 не найден."));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void approve_ShouldReturnBookingDtoWithApprovedStatus() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        BookingDtoResponse bookingDto = bookingService.approve(owner.getId(), addedBooking.getId(), true);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(Status.APPROVED));
    }

    @Test
    @DisplayName("Отмена бронирования")
    void approve_WhenRejected_ShouldReturnBookingDtoWithRejectedStatus() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        BookingDtoResponse bookingDto = bookingService.approve(owner.getId(), addedBooking.getId(), false);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(Status.REJECTED));

    }

    @Test
    @DisplayName("Подтверждение бронирования не владельцем вещи")
    void approve_WhenNotOwnerTryToApprove_ShouldThrowNotAuthorizedException() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approve(booker.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Изменить статус может только владелец!"));
    }

    @Test
    @DisplayName("Подтверждение бронирования с неверным статусом")
    void approve_WhenBookingStatusIsNotWaiting_ShouldThrowItemUnavailableException() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        bookingService.approve(owner.getId(), addedBooking.getId(), true);

        ValidationException e = assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Статус бронирования уже 'APPROVED'"));
    }

    @Test
    @DisplayName("Подтверждение бронирования, пользователь не найден")
    void approve_WhenUserNotFound_ShouldThrowNotFoundException() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approve(999L, addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Пользователь с ID 999 не найден."));
    }

    @Test
    @DisplayName("Подтверждение бронирования, бронирование не найдено")
    void approve_WhenBookingNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approve(owner.getId(), 999L, true));
        assertThat(e.getMessage(), is("Бронь с ID 999 не найдена."));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос от владельца")
    void get_WhenRequestFromOwner_ShouldReturnBookingDto() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        BookingDtoResponse booking = bookingService.get(owner.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(Status.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос пользователя, делающего бронирование")
    void get_WhenRequestFromBooker_ShouldReturnBookingDto() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        BookingDtoResponse booking = bookingService.get(booker.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(Status.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос от другого пользователя")
    void get_WhenRequestFromAnotherUser_ShouldThrowNotAuthorizedException() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        User user3 = User.builder()
                .name("anotherUser")
                .email("anotherUser@mail.com")
                .build();
        User anotherUser = userRepository.save(user3);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.get(anotherUser.getId(), addedBooking.getId()));
        assertThat(e.getMessage(), is("Доступ к бронированию имеет только владелец или автор бронирования!"));
    }

    @Test
    @DisplayName("Получение бронирования по id, пользователь не найден")
    void get_WhenUserNotFound_ShouldThrowNotFoundException() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.get(999L, addedBooking.getId()));
        assertThat(e.getMessage(), is("Пользователь с ID 999 не найден."));
    }

    @Test
    @DisplayName("Получение всех бронирований от владельца, начиная с 1го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStateAllFrom1Size1_ShouldReturnAllBooking() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(owner.getId(), State.ALL, 1L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение всех бронирований, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStateAllFrom0Size1_ShouldReturnAllBooking() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(booker.getId(), State.ALL, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение текущих бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(owner.getId(), State.CURRENT, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение текущих бронирований, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(booker.getId(), State.CURRENT, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение прошедших бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStatePastFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(booker.getId(), State.PAST, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
    @DisplayName("Получение прошедших бронирований, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStatePastFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(owner.getId(), State.PAST, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
    @DisplayName("Получение будущих бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStateFutureFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(owner.getId(), State.FUTURE, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение будущих бронирований, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStateFutureFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(booker.getId(), State.FUTURE, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING от владельца, начиная с 0го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStateWaitingFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);
        bookingService.approve(owner.getId(), addedBooking2.getId(), true);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(owner.getId(), State.WAITING, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStateWaitingFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);
        bookingService.approve(owner.getId(), addedBooking2.getId(), true);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(booker.getId(), State.WAITING, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED от владельца, начиная с 0го элемента по 1 на странице")
    void getAllByOwner_WhenRequesterIsOwnerStateRejectedFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);
        BookingDtoResponse approveBooking = bookingService.approve(owner.getId(), addedBooking.getId(), false);

        List<BookingDtoResponse> bookings = bookingService.getAllByOwner(owner.getId(), State.REJECTED, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(approveBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED, начиная с 0го элемента по 1 на странице")
    void getAllByBooker_WhenRequesterIsBookerStateRejectedFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDtoResponse addedBooking = bookingService.add(booker.getId(), addBookingDto1);
        BookingDtoResponse addedBooking2 = bookingService.add(booker.getId(), addBookingDto2);
        BookingDtoResponse addedBooking3 = bookingService.add(owner.getId(), addBookingDto3);
        BookingDtoResponse approveBooking = bookingService.approve(owner.getId(), addedBooking.getId(), false);

        List<BookingDtoResponse> bookings = bookingService.getAllByBooker(booker.getId(), State.REJECTED, 0L,
                1);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(approveBooking)));
    }
}
