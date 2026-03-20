package com.kirill.filmorate.controller;

import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.service.FilmService;
import com.kirill.filmorate.service.GenreService;
import com.kirill.filmorate.service.MpaRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final GenreService genreService;
    private final MpaRatingService mpaRatingService;

    @Autowired
    public FilmController(FilmService filmService, GenreService genreService, MpaRatingService mpaRatingService) {
        this.filmService = filmService;
        this.genreService = genreService;
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        Film createdFilm = filmService.create(film);
        log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        Film updatedFilm = filmService.update(film);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping("/mpa")
    public Collection<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех MPA рейтингов");
        return mpaRatingService.getAllMpaRatings();
    }

    @GetMapping("/mpa/{id}")
    public MpaRating getMpaRatingById(@PathVariable Long id) {
        log.info("Получен запрос на получение MPA рейтинга с ID: {}", id);
        return mpaRatingService.getMpaRatingById(id);
    }

    @GetMapping("/genres")
    public Collection<Genre> getAllGenres() {
        log.info("Получен запрос на получение всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenreById(@PathVariable Long id) {
        log.info("Получен запрос на получение жанра с ID: {}", id);
        return genreService.getGenreById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: фильм {}, пользователь {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: фильм {}, пользователь {}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }
}