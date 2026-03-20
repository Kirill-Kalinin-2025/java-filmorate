package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.FilmStorage;
import com.kirill.filmorate.storage.UserStorage;
import com.kirill.filmorate.storage.GenreStorage;
import com.kirill.filmorate.storage.MpaRatingStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaRatingStorage mpaRatingStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreStorage genreStorage, MpaRatingStorage mpaRatingStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaRatingStorage = mpaRatingStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        validateFilmExists(film.getId());
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    // Методы для работы с MPA (через отдельный сервис)
    public Collection<MpaRating> getAllMpaRatings() {
        return mpaRatingStorage.findAll();
    }

    public MpaRating getMpaRatingById(Long id) {
        return mpaRatingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с ID " + id + " не найден"));
    }

    // Методы для работы с жанрами (через отдельный сервис)
    public Collection<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }

    // Валидация с оптимизированными запросами
    private void validateFilm(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
    }

    private void validateMpa(MpaRating mpa) {
        if (mpa == null || mpa.getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }

        if (mpaRatingStorage.findById(mpa.getId()).isEmpty()) {
            throw new NotFoundException("MPA рейтинг с ID " + mpa.getId() + " не существует");
        }
    }

    private void validateGenres(Set<Genre> genreSet) {
        if (genreSet == null || genreSet.isEmpty()) {
            return;
        }

        // Проверка на дубликаты
        Set<Long> genreIds = genreSet.stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (genreIds.size() != genreSet.size()) {
            throw new ValidationException("Дублирующиеся жанры");
        }

        // Один запрос к БД для проверки всех жанров
        Collection<Genre> existingGenres = genreStorage.findByIds(genreIds);

        if (existingGenres.size() != genreIds.size()) {
            Set<Long> existingIds = existingGenres.stream().map(Genre::getId).collect(Collectors.toSet());
            genreIds.removeAll(existingIds);
            throw new NotFoundException("Жанр с ID " + genreIds.iterator().next() + " не существует");
        }

        if (genreSet.size() > 5) {
            throw new ValidationException("Фильм не может иметь более 5 жанров");
        }
    }

    // Методы для лайков
    public void addLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmExists(filmId);
        validateUserExists(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        // Один запрос к БД для получения популярных фильмов
        return filmStorage.findPopularFilms(count);
    }

    private void validateFilmExists(Long filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}