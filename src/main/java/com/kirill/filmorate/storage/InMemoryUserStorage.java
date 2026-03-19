package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.User;
import com.kirill.filmorate.model.friendship.FriendshipStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>(); // Для хранения дружбы
    private long nextId = 1;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        friendships.remove(id);
        // Удаляем также все записи, где этот пользователь был другом
        friendships.values().forEach(friendMap -> friendMap.remove(id));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean existsEmail(String email, Long excludeUserId) {
        return users.values().stream()
                .filter(user -> excludeUserId == null || !user.getId().equals(excludeUserId))
                .anyMatch(user -> user.getEmail().equals(email));
    }

    // Новые методы для работы с друзьями
    @Override
    public void addFriend(Long userId, Long friendId) {
        // Односторонняя дружба: добавляем только одну запись
        Map<Long, FriendshipStatus> userFriends = friendships.computeIfAbsent(userId, k -> new HashMap<>());
        userFriends.put(friendId, FriendshipStatus.PENDING);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        if (friendships.containsKey(userId)) {
            friendships.get(userId).put(friendId, FriendshipStatus.CONFIRMED);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
    }

    @Override
    public Collection<Long> getUserFriendIds(Long userId) {
        return friendships.getOrDefault(userId, new HashMap<>()).keySet();
    }

    @Override
    public Collection<Long> getPendingFriendIds(Long userId) {
        return friendships.getOrDefault(userId, new HashMap<>()).entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.PENDING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Long> getConfirmedFriendIds(Long userId) {
        return friendships.getOrDefault(userId, new HashMap<>()).entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}