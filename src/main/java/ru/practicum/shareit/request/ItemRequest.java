package ru.practicum.shareit.request;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * TODO Sprint add-item-requests.
 */
public class ItemRequest {

    long id;
    String description;
    String requestor;
    DateTimeFormat created;
}
