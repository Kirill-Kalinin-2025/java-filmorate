package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.genre.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> findAll();

    Optional<Genre> findById(Long id);

    Collection<Genre> findByIds(Collection<Long> ids);
}