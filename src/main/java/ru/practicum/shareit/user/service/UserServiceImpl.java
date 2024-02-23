package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDtoResponse create(UserDtoRequest request) {
        User user = mapper.toUser(request);
        repository.create(user);

        return mapper.toResponse(user);
    }

    @Override
    public User update(User user) {
        repository.update(user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return repository.getAll();
    }

    @Override
    public User getUserById(Long id) {
        return repository.getUserById(id);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

}