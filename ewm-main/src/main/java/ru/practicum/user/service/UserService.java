package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserDto;

import java.util.List;

public interface UserService {
    NewUserDto createUser(NewUserDto newUserDto);

    List<NewUserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long id);
}
