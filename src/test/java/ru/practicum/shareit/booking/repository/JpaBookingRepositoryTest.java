package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaBookingRepositoryTest {

    @Autowired
    private JpaBookingRepository bookingStorage;

    @Autowired
    private JpaItemRepository itemStorage;

    @Autowired
    private JpaUserRepository userStorage;

    private Item savedItem1;

    private Item savedItem2;

    private User savedUser1;

    private User savedUser2;

    private Booking savedBooking1;

    private Booking savedBooking2;

    private Booking savedBooking3;

    private Booking savedBooking4;

    private OffsetPageRequest pageRequest;

    @BeforeAll
    void setUp() {
        User user1 = createUser(1L);
        savedUser1 = userStorage.save(user1);
        User user2 = createUser(2L);
        savedUser2 = userStorage.save(user2);

        Item item1 = createItem(1L);
        item1.setOwner(savedUser1);
        savedItem1 = itemStorage.save(item1);
        Item item2 = createItem(2L);
        item2.setOwner(savedUser1);
        savedItem2 = itemStorage.save(item2);

        Booking booking1 = createBooking(1L);
        booking1.setItem(savedItem1);
        booking1.setBooker(savedUser2);
        booking1.setStart(now().minusDays(5));
        booking1.setEnd(now().minusDays(1));
        savedBooking1 = bookingStorage.save(booking1);

        Booking booking2 = createBooking(2L);
        booking2.setItem(savedItem1);
        booking2.setBooker(savedUser2);
        booking2.setStart(now().minusDays(1));
        savedBooking2 = bookingStorage.save(booking2);

        Booking booking3 = createBooking(3L);
        booking3.setItem(savedItem2);
        booking3.setBooker(savedUser1);
        savedBooking3 = bookingStorage.save(booking3);
        pageRequest = OffsetPageRequest.of(0L, 1);

        Booking booking4 = createBooking(3L);
        booking4.setItem(savedItem2);
        booking4.setBooker(savedUser2);
        booking4.setStart(now().plusDays(1));
        booking4.setEnd(now().plusDays(5));
        savedBooking4 = bookingStorage.save(booking4);

    }

    @AfterAll
    public void cleanDb() {
        bookingStorage.deleteAll();
        itemStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    @DisplayName("Поиск бронирований по id пользователя, делающего бронирование")
    void findAllByBookerIdOrderByStartDesc_WithPageable_ShouldReturnListOfBookingsOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByBookerIdOrderByStartDesc(savedUser2.getId(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking4.getId())));
    }

    @Test
    @DisplayName("Поиск прошедших бронирований по id пользователя, делающего бронирование")
    void findAllByBookerIdAndEndIsBeforeOrderByStartDesc_ShouldReturnListOfBookingWhereEndBeforeNowWithPageableOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(savedUser2.getId(), now(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking1.getId())));
    }

    @Test
    @DisplayName("Поиск будущих бронирований по id пользователя, делающего бронирование")
    void findAllByBookerIdAndStartIsAfterOrderByStartDesc_ShouldReturnBookingWhereStartIsAfterNowWithPageableOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStartIsAfterOrderByStartDesc(savedUser2.getId(), now(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking4.getId())));
    }

    @Test
    @DisplayName("Поиск текущих бронирований по id пользователя, делающего бронирование")
    void findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc_ShouldReturnListOfBookingWhereStartIsBeforeAndEndIsAfterOrderByStartDesc() {
        savedBooking2.setEnd(now().minusDays(1));
        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(savedUser2.getId(), now(), now().plusDays(1), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking2.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по id пользователя, делающего бронирование и статусу бронирования")
    void findAllByBookerIdAndStatusIsOrderByStartDesc_ShouldReturnListOfBookingWithStatusWaitingWithPageable() {
        List<Booking> bookings = bookingStorage.findAllByBookerIdAndStatusIsOrderByStartDesc(savedUser1.getId(), Status.WAITING,
                pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по id владельца")
    void findAllByItemOwnerIdOrderByStartDesc_WithPageable_ShouldReturnListOfBookingsOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdOrderByStartDesc(savedUser1.getId(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
    }

    @Test
    @DisplayName("Поиск прошедших бронирований по id владельца")
    void findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc_ShouldReturnListOfBookingWhereEndBeforeNowWithPageableOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(savedUser1.getId(), now(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking1.getId())));
    }

    @Test
    @DisplayName("Поиск будущих бронирований по id владельца")
    void findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc_ShouldReturnBookingWhereStartIsAfterNowWithPageableOrderByStartDesc() {
        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(savedUser1.getId(), now(), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
    }

    @Test
    @DisplayName("Поиск текущих бронирований по id владельца")
    void findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc_ShouldReturnListOfBookingWhereStartIsBeforeAndEndIsAfterOrderByStartDesc() {
        savedBooking2.setEnd(now().minusDays(1));
        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(savedUser1.getId(), now(), now().plusDays(1), pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking2.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по id владельца")
    void findAllByItemOwnerIdAndStatusIsOrderByStartDesc_ShouldReturnListOfBookingWithStatusWaitingWithPageable() {
        List<Booking> bookings = bookingStorage.findAllByItemOwnerIdAndStatusIsOrderByStartDesc(savedUser1.getId(), Status.WAITING,
                pageRequest);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(1));
        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по исключающему статусу")
    void findByItemIdInAndStatusNot_ShouldReturnListOfBookingWithStatusNotREJECTED() {
        List<Booking> bookings = bookingStorage.findByItemIdInAndStatusNot(List.of(savedItem1.getId(), savedItem2.getId()), Status.REJECTED);

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), is(4));
        assertThat(bookings.get(0).getId(), is(is(savedBooking1.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по id вещи и id пользователя делающего бронирование")
    void findFirstByItemIdAndBookerIdOrderByStart_ShouldReturnListOfBookings() {
        Optional<Booking> booking = bookingStorage.findFirstByItemIdAndBookerIdOrderByStart(savedItem1.getId(), savedUser2.getId());

        assertTrue(booking.isPresent());
        assertThat(booking.get().getId(), is(is(savedBooking1.getId())));
    }

    @Test
    @DisplayName("Поиск бронирований по id вещи и id пользователя делающего бронирование, бронирование не найдено")
    void findFirstByItemIdAndBookerIdOrderByStart_ShouldReturnEmptyOptional() {
        Optional<Booking> optionalBooking = bookingStorage.findFirstByItemIdAndBookerIdOrderByStart(999L, 999L);

        assertTrue(optionalBooking.isEmpty());
    }

//
//    @Test
//    @DisplayName("Поиск бронирований по списку id вещей")
//    void findAllByItemIdIn_ShouldReturnListOfBookings() {
//        List<Booking> bookings = bookingStorage.findAllByItemIdIn(List.of(savedItem1.getId(), savedItem2.getId()));
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(3));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking1.getId())));
//        assertThat(bookings.get(1).getId(), is(is(savedBooking2.getId())));
//        assertThat(bookings.get(2).getId(), is(is(savedBooking3.getId())));
//    }
//
//    @Test
//    @DisplayName("Поиск бронирований по списку неизвестных id вещей")
//    void findAllByItemIdIn_UnknownId_ShouldReturnEmptyList() {
//        List<Booking> bookings = bookingStorage.findAllByItemIdIn(List.of(444L, 999L));
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(0));
//    }
//
//    @Test
//    @DisplayName("Поиск бронирований по id владельца вещей")
//    void findAllByItemOwnerId_WithPageable_ShouldReturnListOfBookingsOrderByStartDesc() {
//        List<Booking> bookings = bookingStorage.findAllByItemOwnerId(savedUser1.getId(), pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
//    }
//
//    @Test
//    @DisplayName("Поиск бронирований по id владельца вещей, пользователь не найден")
//    void findAllByItemOwnerId_OwnerNotFoundWithPageable_ShouldReturnEmptyList() {
//        List<Booking> bookings = bookingStorage.findAllByItemOwnerId(999L, pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings, emptyIterable());
//    }
//
//    @Test
//    @DisplayName("Поиск текущих бронирований по id владельца вещей")
//    void findCurrentBookingsByOwnerId_ShouldReturnListOfBookingWhereStartIsBeforeNowAndEndAfterNowWithPageable() {
//        List<Booking> bookings = bookingStorage.findCurrentBookingsByOwnerId(savedUser1.getId(), now(), now(),
//                pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking2.getId())));
//    }
//

//

//
//    @Test
//    @DisplayName("Поиск бронирований по id владельца вещей и статусу бронирования")
//    void findBookingsByOwnerIdAndStatus_ShouldReturnListOfBookingWithStatusWaitingWithPageable() {
//        List<Booking> bookings = bookingStorage.findBookingsByOwnerIdAndStatus(savedUser1.getId(), BookingStatus.WAITING,
//                pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
//    }
//

    //
//    @Test
//    @DisplayName("Поиск текущих бронирований по id пользователя, делающего бронирование")
//    void findCurrentBookingsByBookerId_ShouldReturnListOfBookingWhereStartIsBeforeNowAndEndAfterNowWithPageable() {
//        List<Booking> bookings = bookingStorage.findCurrentBookingsByBookerId(savedUser2.getId(), now(), now(),
//                pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking2.getId())));
//    }
//
//    @Test
//    @DisplayName("Поиск прошедших бронирований по id пользователя, делающего бронирование")
//    void findPastBookingsByBookerId_ShouldReturnListOfBookingWhereEndBeforeNowWithPageable() {
//        List<Booking> bookings = bookingStorage.findPastBookingsByBookerId(savedUser2.getId(), now(), pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking1.getId())));
//    }
//
//    @Test
//    @DisplayName("Поиск будущих бронирований по id пользователя, делающего бронирование")
//    void findFutureBookingsByBookerId_ShouldReturnBookingWhereStartIsAfterNowWithPageable() {
//        List<Booking> bookings = bookingStorage.findFutureBookingsByOwnerId(savedUser1.getId(), now(), pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
//    }
//
//    @Test
//    @DisplayName("Поиск бронирований по id пользователя, делающего бронирование и статусу")
//    void findBookingsByBookerIdAndStatus_ShouldReturnListOfBookingWithStatusWaitingWithPageable() {
//        List<Booking> bookings = bookingStorage.findBookingsByBookerIdAndStatus(savedUser1.getId(), BookingStatus.WAITING,
//                pageRequest);
//
//        assertThat(bookings, notNullValue());
//        assertThat(bookings.size(), is(1));
//        assertThat(bookings.get(0).getId(), is(is(savedBooking3.getId())));
//    }
//
    private Item createItem(Long id) {
        return Item.builder()
                .name("name" + id)
                .description("description" + id)
                .available(true)
                .build();
    }

    private User createUser(Long id) {
        return User.builder()
                .name("name" + id)
                .email("email" + id + "@mail.com")
                .build();
    }

    private Booking createBooking(Long id) {
        return Booking.builder()
                .status(Status.WAITING)
                .start(now().plusDays(id))
                .end(now().plusDays(5 + id))
                .build();
    }
}
