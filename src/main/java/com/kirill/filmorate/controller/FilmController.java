package com.kirill.filmorate.controller;

import com.kirill.filmorate.exception.ValidationException;
import com.kirill.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов. Количество фильмов: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);

        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с ID: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);

        if (film.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с ID " + film.getId() + " не найден");
        }

        Film existingFilm = films.get(film.getId());
        updateFilmFields(existingFilm, film);
        log.info("Фильм с ID {} успешно обновлен", film.getId());
        return existingFilm;
    }

    private void updateFilmFields(Film existingFilm, Film newFilm) {
        existingFilm.setName(newFilm.getName());
        existingFilm.setDescription(newFilm.getDescription() != null && !newFilm.getDescription().isBlank()
                ? newFilm.getDescription() : existingFilm.getDescription());
        existingFilm.setReleaseDate(newFilm.getReleaseDate());
        existingFilm.setDuration(newFilm.getDuration());
    }
}