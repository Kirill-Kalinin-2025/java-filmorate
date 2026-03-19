package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.FilmStorage;
import com.kirill.filmorate.storage.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Map<Long, MpaRating> mpaRatings = new HashMap<>();
    private final Map<Long, Genre> genres = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        initializeMpaRatings();
        initializeGenres();
    }

    private void initializeMpaRatings() {
        mpaRatings.put(1L, new MpaRating(1L, "G", "Нет возрастных ограничений"));
        mpaRatings.put(2L, new MpaRating(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"));
        mpaRatings.put(3L, new MpaRating(3L, "PG-13", "Детям до 13 лет просмотр не желателен"));
        mpaRatings.put(4L, new MpaRating(4L, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"));
        mpaRatings.put(5L, new MpaRating(5L, "NC-17", "Лицам до 18 лет просмотр запрещён"));
    }

    private void initializeGenres() {
        genres.put(1L, new Genre(1L, "Комедия"));
        genres.put(2L, new Genre(2L, "Драма"));
        genres.put(3L, new Genre(3L, "Мультфильм"));
        genres.put(4L, new Genre(4L, "Триллер"));
        genres.put(5L, new Genre(5L, "Документальный"));
        genres.put(6L, new Genre(6L, "Боевик"));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film);
        enrichFilmWithData(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        validateFilmExists(film.getId());
        validateFilm(film);
        enrichFilmWithData(film);
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public Collection<MpaRating> getAllMpaRatings() {
        return new ArrayList<>(mpaRatings.values());
    }

    public MpaRating getMpaRatingById(Long id) {
        MpaRating mpa = mpaRatings.get(id);
        if (mpa == null) {
            throw new NotFoundException("MPA рейтинг с ID " + id + " не найден");
        }
        return mpa;
    }

    public Collection<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    public Genre getGenreById(Long id) {
        Genre genre = genres.get(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        return genre;
    }

    private void enrichFilmWithData(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating fullMpa = mpaRatings.get(film.getMpa().getId());
            if (fullMpa != null) {
                film.getMpa().setName(fullMpa.getName());
                film.getMpa().setDescription(fullMpa.getDescription());
            }
        }
    }

    private void validateFilm(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
    }

    private void validateMpa(MpaRating mpa) {
        if (mpa == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }
        if (mpa.getId() == null || !mpaRatings.containsKey(mpa.getId())) {
            throw new ValidationException("MPA рейтинг с ID " + (mpa.getId() == null ? "null" : mpa.getId()) + " не существует");
        }
    }

    private void validateGenres(Set<Genre> genreSet) {
        if (genreSet == null) {
            return;
        }

        Set<Long> genreIds = new HashSet<>();
        for (Genre genre : genreSet) {
            if (genre == null || genre.getId() == null) {
                throw new ValidationException("Жанр не может быть null или без ID");
            }

            if (!genreIds.add(genre.getId())) {
                throw new ValidationException("Дублирующийся жанр с ID " + genre.getId());
            }

            if (!genres.containsKey(genre.getId())) {
                throw new ValidationException("Жанр с ID " + genre.getId() + " не существует");
            }
        }

        if (genreSet.size() > 5) {
            throw new ValidationException("Фильм не может иметь более 5 жанров");
        }
    }

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

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        filmStorage.getLikesCount(f2.getId()),
                        filmStorage.getLikesCount(f1.getId())
                ))
                .limit(count)
                .collect(Collectors.toList());
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