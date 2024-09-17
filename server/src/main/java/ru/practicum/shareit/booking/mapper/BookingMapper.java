package ru.practicum.shareit.booking.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDtoResponse toBookingDtoResponse(Booking booking);

    List<BookingDtoResponse> toBookingDtoResponseList(List<Booking> booking);

    @Mapping(target = "id", ignore = true)
    Booking toBooking(BookingDtoRequest bookingDtoRequest, Item item, User booker, Status status);

    @Mapping(target = "bookerId", expression = "java(booking.getBooker().getId())")
    BookingDtoForItem toBookingDtoForItem(Booking booking);
}
