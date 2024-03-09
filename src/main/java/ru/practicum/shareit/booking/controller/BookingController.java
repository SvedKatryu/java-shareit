package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingServiceImpl;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    //public static final String USER_ID = "X-Sharer-User-Id";
    private final BookingServiceImpl bookingService;

    @PostMapping
    public BookingDtoResponse add(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody BookingDtoRequest bookingDtoRequest) {
        log.info("Получен запрос на бронирование вещи {}", bookingDtoRequest);
        return bookingService.add(userId, bookingDtoRequest);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable @Positive long bookingId,
                              @RequestParam(name = "approved") boolean approved) {
        log.info("Получен запрос на смену статуса бронирования {} вещи ", bookingId);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable @Positive long bookingId) {
        log.info("Получен запрос на получение бронирования {} вещи ", bookingId);
        return bookingService.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoResponse> getAllByBooker(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                   @RequestParam(name = "state", defaultValue = "ALL")
                                                   State state) {
        log.info("Получен запрос на получение всех бронирований пользователя ID{}", bookerId);
        return bookingService.getAllByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                          @RequestParam(name = "state", defaultValue = "ALL")
                                          State state) {
        log.info("Получен запрос на получение списка бронирований всех вещей пользователя ID{}", ownerId);
        return bookingService.getAllByOwner(ownerId, state);
    }
}
