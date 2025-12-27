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
        Collection<Film> films = filmStorage.findAll();
        films.forEach(this::enrichFilmWithData);
        return films;
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
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        enrichFilmWithData(film);
        return film;
    }

    public Collection<MpaRating> getAllMpaRatings() {
        return mpaRatings.values().stream()
                .sorted(Comparator.comparing(MpaRating::getId))
                .collect(Collectors.toList());
    }

    public MpaRating getMpaRatingById(Long id) {
        return Optional.ofNullable(mpaRatings.get(id))
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с ID " + id + " не найден"));
    }

    public Collection<Genre> getAllGenres() {
        return genres.values().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }

    public Genre getGenreById(Long id) {
        return Optional.ofNullable(genres.get(id))
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }

    private void enrichFilmWithData(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating fullMpa = mpaRatings.get(film.getMpa().getId());
            if (fullMpa != null) {
                film.getMpa().setName(fullMpa.getName());
                film.getMpa().setDescription(fullMpa.getDescription());
            }
        }

        if (film.getGenres() != null) {
            Set<Genre> enrichedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    Genre fullGenre = genres.get(genre.getId());
                    if (fullGenre != null) {
                        enrichedGenres.add(new Genre(fullGenre.getId(), fullGenre.getName()));
                    }
                }
            }
            film.setGenres(enrichedGenres);
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
            throw new NotFoundException("MPA рейтинг с ID " + mpa.getId() + " не существует");
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
                throw new NotFoundException("Жанр с ID " + genre.getId() + " не существует");
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
                .map(film -> {
                    enrichFilmWithData(film);
                    return film;
                })
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