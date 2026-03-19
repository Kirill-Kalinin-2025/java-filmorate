package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();
    Optional<User> findById(Long id);
    User create(User user);
    User update(User user);
    void delete(Long id);
    boolean existsById(Long id);
    boolean existsEmail(String email, Long excludeUserId);

    // Новые методы для работы с друзьями (односторонняя дружба)
    void addFriend(Long userId, Long friendId);
    void confirmFriend(Long userId, Long friendId);
    void removeFriend(Long userId, Long friendId);
    Collection<Long> getUserFriendIds(Long userId);
    Collection<Long> getPendingFriendIds(Long userId);
    Collection<Long> getConfirmedFriendIds(Long userId);
}