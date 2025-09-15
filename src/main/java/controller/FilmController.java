package controller;

import exception.ValidationException;
import model.Film;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.time.LocalDate;
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

        validateFilm(film);
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

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null) {
            LocalDate minDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(minDate)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        }
    }

    private void updateFilmFields(Film existingFilm, Film newFilm) {
        if (newFilm.getName() != null) existingFilm.setName(newFilm.getName());
        if (newFilm.getDescription() != null) existingFilm.setDescription(newFilm.getDescription());
        if (newFilm.getReleaseDate() != null) existingFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) existingFilm.setDuration(newFilm.getDuration());
    }
}