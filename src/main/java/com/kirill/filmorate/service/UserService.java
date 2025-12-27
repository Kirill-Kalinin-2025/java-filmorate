package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.User;
import com.kirill.filmorate.model.friendship.FriendshipStatus;
import com.kirill.filmorate.storage.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>();

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
        validateUserExists(user.getId());
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

        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить сам себя в друзья");
        }

        Map<Long, FriendshipStatus> userFriends = friendships.computeIfAbsent(userId, k -> new HashMap<>());
        Map<Long, FriendshipStatus> friendFriends = friendships.computeIfAbsent(friendId, k -> new HashMap<>());

        if (userFriends.containsKey(friendId) || friendFriends.containsKey(userId)) {
            throw new ValidationException("Пользователи уже друзья");
        }

        userFriends.put(friendId, FriendshipStatus.CONFIRMED);
        friendFriends.put(userId, FriendshipStatus.CONFIRMED);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может удалить сам себя из друзей");
        }

        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
            // Если у пользователя больше нет друзей, удаляем запись
            if (friendships.get(userId).isEmpty()) {
                friendships.remove(userId);
            }
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
            // Если у друга больше нет друзей, удаляем запись
            if (friendships.get(friendId).isEmpty()) {
                friendships.remove(friendId);
            }
        }
    }

    public Collection<User> getFriends(Long userId) {
        validateUserExists(userId);
        Map<Long, FriendshipStatus> userFriends = friendships.getOrDefault(userId, new HashMap<>());

        // Возвращаем только CONFIRMED друзей
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);

        if (userId.equals(otherId)) {
            throw new ValidationException("ID пользователей должны быть разными");
        }

        Set<Long> userFriends = friendships.getOrDefault(userId, new HashMap<>())
                .entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Long> otherFriends = friendships.getOrDefault(otherId, new HashMap<>())
                .entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        userFriends.retainAll(otherFriends);

        return userFriends.stream()
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

    public void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}