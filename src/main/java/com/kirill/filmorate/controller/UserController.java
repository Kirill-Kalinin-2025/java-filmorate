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

        processUserCreation(user);
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

        User existingUser = users.get(user.getId());
        updateUserFields(existingUser, user);
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return existingUser;
    }

    private void processUserCreation(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        // Проверка на уникальность email или логина
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
    }

    private void updateUserFields(User existingUser, User newUser) {
        if (newUser.getEmail() != null) existingUser.setEmail(newUser.getEmail());
        if (newUser.getLogin() != null) existingUser.setLogin(newUser.getLogin());
        if (newUser.getName() != null) existingUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) existingUser.setBirthday(newUser.getBirthday());
    }
}