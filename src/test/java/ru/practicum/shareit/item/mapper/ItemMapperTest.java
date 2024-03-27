package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemMapperTest {

    private ItemMapper itemMapper;

    @BeforeAll
    void init() {
        itemMapper = new ItemMapperImpl();
    }

    @Test
    @DisplayName("Маппинг null toDto")
    void toDto_mapNull_ShouldReturnNull() {
        Item dto = itemMapper.toItem(null);

        assertThat(dto, nullValue());
    }

    @Test
    @DisplayName("Маппинг null toModel")
    void toItemDto_mapNull_ShouldReturnNull() {
        ItemDto item = itemMapper.toItemWithRequest(null);

        assertThat(item, nullValue());
    }

    @Test
    @DisplayName("Маппинг null toItemDtoResponse")
    void toItemDtoResponse_mapNull_ShouldReturnNull() {
        ItemDtoResponse itemDto = itemMapper.toResponse(null);

        assertThat(itemDto, nullValue());
    }
}
