package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.FilmStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private final Map<Long, MpaRating> mpaRatings = new HashMap<>();
    private final Map<Long, Genre> genres = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        initializeMpaRatings();
        initializeGenres();
    }

    private void initializeMpaRatings() {
        mpaRatings.put(1L, MpaRating.G);
        mpaRatings.put(2L, MpaRating.PG);
        mpaRatings.put(3L, MpaRating.PG_13);
        mpaRatings.put(4L, MpaRating.R);
        mpaRatings.put(5L, MpaRating.NC_17);
    }

    private void initializeGenres() {
        genres.put(1L, Genre.COMEDY);
        genres.put(2L, Genre.DRAMA);
        genres.put(3L, Genre.CARTOON);
        genres.put(4L, Genre.THRILLER);
        genres.put(5L, Genre.DOCUMENTARY);
        genres.put(6L, Genre.ACTION);
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

    public Collection<MpaRating> getAllMpaRatings() {
        return mpaRatings.values();
    }

    public MpaRating getMpaRatingById(Long id) {
        return Optional.ofNullable(mpaRatings.get(id))
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с ID " + id + " не найден"));
    }

    public Collection<Genre> getAllGenres() {
        return genres.values();
    }

    public Genre getGenreById(Long id) {
        return Optional.ofNullable(genres.get(id))
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
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
            throw new ValidationException("MPA рейтинг с ID " + mpa.getId() + " не существует");
        }
    }

    private void validateGenres(Set<Genre> genreSet) {
        if (genreSet == null) {
            throw new ValidationException("Список жанров не может быть null");
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