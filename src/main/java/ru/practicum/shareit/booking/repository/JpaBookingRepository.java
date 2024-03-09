package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBookingRepository extends JpaRepository<Booking, Long> {

        List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

        List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

        List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

        List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
        Long bookerId, LocalDateTime now1, LocalDateTime now2);

        List<Booking> findAllByBookerIdAndStatusIsOrderByStartDesc(Long bookerId, Status bookingStatus);

        List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

        List<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

        List<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

        List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
        Long ownerId, LocalDateTime now1, LocalDateTime now2);

        List<Booking> findAllByItemOwnerIdAndStatusIsOrderByStartDesc(Long ownerId, Status bookingStatus);

        List<Booking> findByItemIdInAndStatusNot(List<Long> itemIds, Status bookingStatus);

        Optional<Booking> findFirstByItemIdAndBookerIdOrderByStart(Long itemId, Long bookerId);
        }