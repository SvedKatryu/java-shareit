package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingMapperTest {

    private BookingMapper bookingMapper;

    User booker;
    Booking booking;

    private Item item;

    BookingDtoRequest bookingDtoRequest;

    @BeforeAll
    void setUp() {
        long itemId = 3L;
        long bookingId = 2L;
        bookingMapper = new BookingMapperImpl();

        booker = User.builder()
                .id(1L)
                .name("name")
                .email("email@email.com")
                .build();
        booking = Booking.builder()
                .id(bookingId)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();
        item = Item.builder()
                .id(itemId)
                .name("name")
                .description("description")
                .available(true)
                .build();
        item.setOwner(booker);

        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();

    }

    @Test
    @DisplayName("Проверка маппинга toBookingDtoForItem")
    void mapToBookingDtoForItem() {

        BookingDtoForItem bookingDtoForItem = bookingMapper.toBookingDtoForItem(booking);

        assertThat(bookingDtoForItem.getBookerId(), is(booker.getId()));
        assertThat(bookingDtoForItem.getStart(), is(booking.getStart()));
        assertThat(bookingDtoForItem.getEnd(), is(booking.getEnd()));
        assertThat(bookingDtoForItem.getId(), is(booking.getId()));
    }

    @Test
    @DisplayName("Маппинг null toBookingDtoForItem")
    void mapToBookingDtoForItem_mapNull_ShouldReturnNull() {
        BookingDtoForItem bookingDtoForItem = bookingMapper.toBookingDtoForItem(null);

        assertThat(bookingDtoForItem, nullValue());
    }

    @Test
    @DisplayName("Проверка маппинга toBooking")
    void mapToBooking() {

        Booking booking = bookingMapper.toBooking(bookingDtoRequest, item, booker, Status.WAITING);

        assertThat(booking.getBooker(), is(booker));
        assertThat(booking.getStart(), is(bookingDtoRequest.getStart()));
        assertThat(booking.getEnd(), is(bookingDtoRequest.getEnd()));
        assertThat(booking.getItem(), is(item));
        assertThat(booking.getStatus(), is(Status.WAITING));
    }

    @Test
    @DisplayName("Маппинг null toBooking")
    void mapToBooking_mapNull_ShouldReturnNull() {
        Booking booking = bookingMapper.toBooking(null, null, null, null);

        assertThat(booking, nullValue());
    }

    @Test
    @DisplayName("Проверка маппинга toBooking")
    void mapToBooking_bookingDtoRequestNull_ShouldReturnNull() {

        Booking booking = bookingMapper.toBooking(null, item, booker, Status.WAITING);

        assertThat(booking.getBooker(), is(booker));
        assertThat(booking.getStart(), nullValue());
        assertThat(booking.getEnd(), nullValue());
        assertThat(booking.getItem(), is(item));
        assertThat(booking.getStatus(), is(Status.WAITING));
    }

    @Test
    @DisplayName("Проверка маппинга toBooking")
    void mapToBooking_itemNull_ShouldReturnNull() {

        Booking booking = bookingMapper.toBooking(bookingDtoRequest, null, booker, Status.WAITING);

        assertThat(booking.getBooker(), is(booker));
        assertThat(booking.getStart(), is(booking.getStart()));
        assertThat(booking.getEnd(), is(booking.getEnd()));
        assertThat(booking.getId(), is(booking.getId()));
        assertThat(booking.getItem(), nullValue());
        assertThat(booking.getStatus(), is(Status.WAITING));
    }

    @Test
    @DisplayName("Проверка маппинга toBooking")
    void mapToBooking_bookerNull_ShouldReturnNull() {

        Booking booking = bookingMapper.toBooking(bookingDtoRequest, item, null, Status.WAITING);

        assertThat(booking.getBooker(), nullValue());
        assertThat(booking.getStart(), is(booking.getStart()));
        assertThat(booking.getEnd(), is(booking.getEnd()));
        assertThat(booking.getId(), is(booking.getId()));
        assertThat(booking.getItem(), is(item));
        assertThat(booking.getStatus(), is(Status.WAITING));
    }

    @Test
    @DisplayName("Проверка маппинга toBooking")
    void mapToBooking_statusNull_ShouldReturnNull() {

        Booking booking = bookingMapper.toBooking(bookingDtoRequest, item, booker, null);

        assertThat(booking.getBooker(), is(booker));
        assertThat(booking.getStart(), is(booking.getStart()));
        assertThat(booking.getEnd(), is(booking.getEnd()));
        assertThat(booking.getId(), is(booking.getId()));
        assertThat(booking.getItem(), is(item));
        assertThat(booking.getStatus(), nullValue());
    }

    @Test
    @DisplayName("Проверка маппинга toBookingDtoResponse")
    void mapToBookingDtoResponse() {

        BookingDtoResponse bookingDtoResponse = bookingMapper.toBookingDtoResponse(booking);

        assertThat(bookingDtoResponse.getStart(), is(booking.getStart()));
        assertThat(bookingDtoResponse.getEnd(), is(booking.getEnd()));
        assertThat(bookingDtoResponse.getId(), is(booking.getId()));
    }

    @Test
    @DisplayName("Маппинг null toBookingDtoResponse")
    void toBookingDtoResponse_mapNull_ShouldReturnNull() {
        BookingDtoResponse bookingDtoResponse = bookingMapper.toBookingDtoResponse(null);

        assertThat(bookingDtoResponse, nullValue());
    }

    @Test
    @DisplayName("Проверка маппинга toBookingDtoResponseList")
    void mapToBookingDtoResponseList() {

        List<BookingDtoResponse> bookingDtoResponseList = bookingMapper.toBookingDtoResponseList(List.of(booking));

        assertThat(bookingDtoResponseList.size(), is(1));
        assertThat(bookingDtoResponseList.get(0).getId(), is(booking.getId()));
    }

    @Test
    @DisplayName("Маппинг null toBookingDtoResponseList")
    void toBookingDtoResponseList_mapNull_ShouldReturnNull() {
        List<BookingDtoResponse> bookingDtoResponseList = bookingMapper.toBookingDtoResponseList(null);

        assertThat(bookingDtoResponseList, nullValue());
    }
}
