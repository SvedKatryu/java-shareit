package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;

@Component
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public UserDtoResponse create(UserDtoRequest request) {
        return post("", request, UserDtoRequest.class, UserDtoResponse.class);
    }

    public UserDtoResponse update(long userId, UserDtoRequest userUpdateDto) {
        return patch("/" + userId, userUpdateDto, UserDtoResponse.class);
    }

    public UserDtoResponse getUserById(long userId) {
        return get("/" + userId, UserDtoResponse.class);
    }

    public List<UserDtoResponse> getAll() {
        return get("", List.class);
    }

    public void delete(long userId) {
        delete("/" + userId);
    }
}
