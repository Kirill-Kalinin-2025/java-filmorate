package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    Optional<Film> findById(Long id);

    boolean existsById(Long id);
}