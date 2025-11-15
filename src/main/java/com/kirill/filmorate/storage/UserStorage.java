package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    void delete(Long id);

    Optional<User> findById(Long id);

    boolean existsById(Long id);

    boolean existsEmail(String email, Long excludeUserId);
}