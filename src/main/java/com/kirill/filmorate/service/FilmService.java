package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.storage.FilmStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        validateFilmExists(film.getId());
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        userService.validateUserExists(userId);

        Set<Long> filmLikes = likes.computeIfAbsent(filmId, k -> new HashSet<>());
        filmLikes.add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        userService.validateUserExists(userId);

        if (likes.containsKey(filmId)) {
            boolean removed = likes.get(filmId).remove(userId);
            if (!removed) {
                throw new ValidationException("Лайк от пользователя " + userId + " для фильма " + filmId + " не найден");
            }
        } else {
            throw new ValidationException("Лайки для фильма " + filmId + " не найдены");
        }
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        getLikesCount(f2.getId()),
                        getLikesCount(f1.getId())
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    private int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, new HashSet<>()).size();
    }

    private void validateFilmExists(Long filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }
}