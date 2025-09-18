package com.kirill.filmorate.controller;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        setUserNameFromLoginIfEmpty(user);
        validateEmailUniqueness(user.getEmail(), null);

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);

        if (user.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с ID " + user.getId() + " не найден");
        }

        setUserNameFromLoginIfEmpty(user);
        validateEmailUniqueness(user.getEmail(), user.getId());

        User existingUser = users.get(user.getId());
        updateUserFields(existingUser, user);
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return existingUser;
    }

    private void setUserNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        boolean emailExists = users.values().stream()
                .filter(u -> currentUserId == null || !u.getId().equals(currentUserId))
                .anyMatch(u -> u.getEmail().equals(email));

        if (emailExists) {
            throw new ValidationException("Пользователь с email " + email + " уже существует");
        }
    }

    private void updateUserFields(User existingUser, User newUser) {
        existingUser.setEmail(newUser.getEmail());
        existingUser.setLogin(newUser.getLogin());
        existingUser.setName(newUser.getName());
        existingUser.setBirthday(newUser.getBirthday());
    }
}