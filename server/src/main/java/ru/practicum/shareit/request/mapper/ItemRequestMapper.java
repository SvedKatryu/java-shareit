package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequest toItemRequest(ItemRequestDtoRequest itemRequestDtoRequest);

    ItemRequestDtoResponse toResponse(ItemRequest itemRequest);

    List<ItemRequestDtoResponse> toDtoResponseList(List<ItemRequest> requests);
}