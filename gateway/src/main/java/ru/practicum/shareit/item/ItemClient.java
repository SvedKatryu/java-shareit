package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;
import java.util.Map;

@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ItemDto addNewItem(Long userId, ItemDtoRequest request) {
        return post("", userId, request, ItemDto.class);
    }

    public ItemDtoResponse update(Long userId, long itemId, ItemDtoRequest itemUpdateDto) {
        return patch("/" + itemId, userId, itemUpdateDto,
                ItemDtoResponse.class);
    }

    public ItemDtoResponse getItemById(Long userId, long itemId) {
        return get("/" + itemId, userId, ItemDtoResponse.class);
    }

    public List<ItemDtoResponse> getItemsByUserId(Long userId, Long from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters, List.class);
    }

    public List<ItemDtoResponse> findItemsByText(Long userId, String text, Long from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters, List.class);
    }

    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto, CommentDto.class);
    }
}
