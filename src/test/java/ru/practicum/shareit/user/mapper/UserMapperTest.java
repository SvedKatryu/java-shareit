package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        UserMapperImpl.class,
        UserMapperImpl.class})
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;


    @Test
    @DisplayName("Маппинг toUser")
    public void mapToUserNotNull() {
        UserDtoRequest userDtoRequest = UserDtoRequest.builder().name("name").email("email").build();

        User user = userMapper.toUser(userDtoRequest);

        assertThat(user.getName(), is(userDtoRequest.getName()));
        assertThat(user.getEmail(), is(userDtoRequest.getEmail()));
    }

    @Test
    @DisplayName("Маппинг null mapToUserl")
    void mapToUserNull() {
        User user = userMapper.toUser(null);

        assertThat(user, nullValue());
    }

    @Test
    @DisplayName("Маппинг toResponse")
    public void toResponseNotNull() {
        User user = User.builder().name("name").email("email").build();

        UserDtoResponse userDto = userMapper.toResponse(user);

        assertThat(userDto.getName(), is(user.getName()));
        assertThat(userDto.getEmail(), is(user.getEmail()));
    }

    @Test
    @DisplayName("Маппинг null toResponse")
    void maptoResponseNull() {

        UserDtoResponse userDto = userMapper.toResponse(null);

        assertThat(null, nullValue());
    }

}
