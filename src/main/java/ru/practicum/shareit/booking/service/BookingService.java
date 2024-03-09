package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingDtoResponse add(Long userId, BookingDtoRequest bookingDtoRequest);

    BookingDtoResponse approve(Long userId, Long bookingId, boolean approve);

    BookingDtoResponse get(Long userId, Long bookingId);

    List<BookingDtoResponse> getAllByBooker(Long booker, State state);

    List<BookingDtoResponse> getAllByOwner(Long ownerId, State state);
}
