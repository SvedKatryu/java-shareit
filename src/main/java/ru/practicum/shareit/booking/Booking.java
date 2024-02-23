package ru.practicum.shareit.booking;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
public class Booking {
    protected long id;
    protected LocalDateTime start;
    protected LocalDateTime end;
    protected String item;
    protected String booker;
    protected Status status;

}
