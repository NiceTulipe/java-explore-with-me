package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.dao.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.user.mapper.UserMapper.toUser;
import static ru.practicum.user.mapper.UserMapper.toUserDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Transactional
    public NewUserDto createUser(NewUserDto newUserDto) {
        User user = toUser(newUserDto);
        try {
            return toUserDto(repository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email " + newUserDto.getEmail() + " or Name " +
                    newUserDto.getName() + " already in use");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Request for add new user " + newUserDto + " incorrect ");
        }
    }

    public List<NewUserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        if (ids == null) {
            return repository.findAll(page).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findByIdIn(ids, page).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}