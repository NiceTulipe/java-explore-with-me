package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewUserDto createUser(@RequestBody @Valid NewUserDto newUserDto) {
        log.info("Create user {}", newUserDto);
        return userService.createUser(newUserDto);
    }

    @GetMapping
    public List<NewUserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get users with ids {}, from {}, size {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Delete user {}", id);
        userService.deleteUser(id);
    }
}
