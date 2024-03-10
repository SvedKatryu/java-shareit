package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public abstract class BookingMapperDecorator implements BookingMapper {
    BookingMapper bookingMapper;

    @Override
    public Booking toBooking(BookingDtoRequest bookingDtoRequest, Item item, User booker, Status status) {
        Booking booking = bookingMapper.toBooking(bookingDtoRequest, item, booker, status);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}