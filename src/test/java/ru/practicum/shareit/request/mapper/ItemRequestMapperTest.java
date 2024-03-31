package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ItemMapperImpl.class,
        ItemRequestMapperImpl.class})
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    @DisplayName("Маппинг ItemRequestDtoResponse в ItemRequest, проверка даты создания")
    public void mapToItemRequestWithCreatedNotNull() {
        ItemRequestDtoRequest addItemRequestDto = new ItemRequestDtoRequest("description");

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(addItemRequestDto);

        assertThat(itemRequest.getDescription(), is(addItemRequestDto.getDescription()));
        assertThat(itemRequest.getCreated(), is(notNullValue()));
    }

    @Test
    @DisplayName("Маппинг null ItemRequestDtoResponse в ItemRequest, проверка даты создания")
    void mapToItemRequestWithCreatedNull() {
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(null);

        assertThat(itemRequest, nullValue());
    }

    @Test
    @DisplayName("Маппинг ItemRequest в ItemRequestDtoResponse, проверка id пользователя")
    public void mapToResponseDtoWithItem() {
        User requester = User.builder().id(1L).build();
        Item item = Item.builder().id(2L).build();
        ItemRequest itemRequest = ItemRequest.builder().id(1L).requester(requester).build();
        itemRequest.addItem(item);

        ItemRequestDtoResponse itemRequestDto = itemRequestMapper.toResponse(itemRequest);

        assertThat(itemRequestDto.getItems().get(0).getId(), is(2L));
    }

    @Test
    @DisplayName("Маппинг null ItemRequest в ItemRequestDtoResponse, проверка id пользователя")
    public void mapToResponseDtoWithItemNull() {
        ItemRequestDtoResponse itemRequestDto = itemRequestMapper.toResponse(null);

        assertThat(itemRequestDto, nullValue());
    }

    @Test
    @DisplayName("Маппинг null Item в ItemRequestDtoResponseList, проверка id пользователя")
    public void mapToDtoResponseListWithItemNull() {

        List<ItemRequestDtoResponse> itemRequestDtos = itemRequestMapper.toDtoResponseList(null);

        assertThat(itemRequestDtos, nullValue());
    }

    @Test
    @DisplayName("Маппинг Item в ItemRequestDtoResponseList, проверка id пользователя")
    public void mapToDtoResponseListWithItem() {
        User requester = User.builder().id(1L).build();
        Item item = Item.builder().id(2L).build();
        ItemRequest itemRequest = ItemRequest.builder().id(1L).requester(requester).build();
        itemRequest.addItem(item);

        List<ItemRequestDtoResponse> itemRequestDtos = itemRequestMapper.toDtoResponseList(List.of(itemRequest));

        assertThat(itemRequestDtos.get(0).getItems().get(0).getId(), is(2L));
    }

}
