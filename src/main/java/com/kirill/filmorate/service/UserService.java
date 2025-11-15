package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.User;
import com.kirill.filmorate.storage.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friendships = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        setUserNameFromLoginIfEmpty(user);
        validateEmailUniqueness(user.getEmail(), null);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        if (!userStorage.existsById(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        setUserNameFromLoginIfEmpty(user);
        validateEmailUniqueness(user.getEmail(), user.getId());
        return userStorage.update(user);
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        friendships.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friendships.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
        }
    }

    public Collection<User> getFriends(Long userId) {
        validateUserExists(userId);
        Set<Long> friendIds = friendships.getOrDefault(userId, new HashSet<>());
        return friendIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);

        Set<Long> userFriends = friendships.getOrDefault(userId, new HashSet<>());
        Set<Long> otherFriends = friendships.getOrDefault(otherId, new HashSet<>());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void setUserNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        if (currentUserId != null) {
            User currentUser = userStorage.findById(currentUserId).orElse(null);
            if (currentUser != null && currentUser.getEmail().equals(email)) {
                return;
            }
        }

        if (userStorage.existsEmail(email, currentUserId)) {
            throw new ValidationException("Пользователь с email " + email + " уже существует");
        }
    }

    void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}