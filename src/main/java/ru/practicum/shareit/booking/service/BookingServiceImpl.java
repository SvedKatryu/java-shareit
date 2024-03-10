package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final JpaBookingRepository bookingRepository;
    private final JpaUserRepository userRepository;
    private final JpaItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDtoResponse add(Long userId, BookingDtoRequest bookingDtoRequest) {
        if (bookingDtoRequest.getStart().isAfter(bookingDtoRequest.getEnd()) ||
                bookingDtoRequest.getStart().equals((bookingDtoRequest.getEnd()))) {
            throw new ValidationException(String.format("Время начала = %s или конца = %s бронирования неверное",
                    bookingDtoRequest.getStart(), bookingDtoRequest.getEnd()));
        }
        Item item = itemRepository.findById(bookingDtoRequest.getItemId()).orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) throw new ValidationException("Вещь не доступна для бронирования.");
        if (userId.equals(item.getOwner().getId()))
            throw new NotFoundException("Владелец не может забронировать свою вещь.");
        User booker = userIsPresent(userId);
        Booking booking = bookingMapper.toBooking(bookingDtoRequest, item, booker, Status.WAITING);
        bookingRepository.save(booking);
        return bookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoResponse approve(Long userId, Long bookingId, boolean approve) {
        Booking booking = bookingIsPresent(bookingId);
        if (!userId.equals(booking.getItem().getOwner().getId()))
            throw new NotFoundException("Изменить статус может только владелец!");
        if (booking.getStatus().equals(Status.APPROVED))
            throw new ValidationException("Статус бронирования уже 'APPROVED'");
        booking.setStatus(approve ? Status.APPROVED : Status.REJECTED);
        return bookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoResponse get(Long userId, Long bookingId) {
        Booking booking = bookingIsPresent(bookingId);
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId()))
            throw new NotFoundException("Доступ к бронированию имеет только владелец или автор бронирования!");
        return bookingMapper.toBookingDtoResponse(booking);
    }

    @Override
    @Transactional
    public List<BookingDtoResponse> getAllByBooker(Long bookerId, State state) {
        userIsPresent(bookerId);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return bookingMapper.toBookingDtoResponse(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId));
            case PAST:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now));
            case FUTURE:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now));
            case CURRENT:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now));
            case WAITING:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdAndStatusIsOrderByStartDesc(bookerId, Status.WAITING));
            case REJECTED:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByBookerIdAndStatusIsOrderByStartDesc(bookerId, Status.REJECTED));
            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    @Transactional
    public List<BookingDtoResponse> getAllByOwner(Long ownerId, State state) {
        userIsPresent(ownerId);
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return bookingMapper.toBookingDtoResponse(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId));
            case PAST:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(ownerId, now));
            case FUTURE:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(ownerId, now));
            case CURRENT:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now));
            case WAITING:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByItemOwnerIdAndStatusIsOrderByStartDesc(ownerId, Status.WAITING));
            case REJECTED:
                return bookingMapper.toBookingDtoResponse(bookingRepository
                        .findAllByItemOwnerIdAndStatusIsOrderByStartDesc(ownerId, Status.REJECTED));
            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private User userIsPresent(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Booking bookingIsPresent(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронь не найдена"));
    }
}
