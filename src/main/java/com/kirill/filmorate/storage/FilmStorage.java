package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    boolean existsById(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Long> getFilmLikes(Long filmId);

    int getLikesCount(Long filmId);

    void addFilmGenres(Long filmId, Collection<Long> genreIds);

    void removeAllFilmGenres(Long filmId);

    Collection<Long> getFilmGenreIds(Long filmId);

    Collection<Film> findPopularFilms(int count);
}