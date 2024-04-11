package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingServiceImpl;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingServiceImpl bookingService;

    private static final String DEFAULT_PAGE_SIZE = "10";

    @PostMapping
    public BookingDtoResponse add(@RequestHeader("X-Sharer-User-Id") long userId, @Validated @RequestBody BookingDtoRequest bookingDtoRequest) {
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
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                   @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("Получен запрос на получение всех бронирований пользователя ID{}", bookerId);
        State currentState = State.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));

        return bookingService.getAllByBooker(bookerId, currentState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                  @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("Получен запрос на получение списка бронирований всех вещей пользователя ID{}", ownerId);
        State currentState = State.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingService.getAllByOwner(ownerId, currentState, from, size);
    }
}
