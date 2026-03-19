package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private final Map<Long, Set<Long>> filmGenres = new HashMap<>(); // filmId -> Set of genreIds
    private final Map<Long, Genre> allGenres = new HashMap<>(); // Справочник всех жанров
    private final Map<Long, MpaRating> allMpaRatings = new HashMap<>(); // Справочник всех MPA
    private long nextId = 1;

    public InMemoryFilmStorage() {
        initializeGenres();
        initializeMpaRatings();
    }

    private void initializeGenres() {
        allGenres.put(1L, new Genre(1L, "Комедия"));
        allGenres.put(2L, new Genre(2L, "Драма"));
        allGenres.put(3L, new Genre(3L, "Мультфильм"));
        allGenres.put(4L, new Genre(4L, "Триллер"));
        allGenres.put(5L, new Genre(5L, "Документальный"));
        allGenres.put(6L, new Genre(6L, "Боевик"));
    }

    private void initializeMpaRatings() {
        allMpaRatings.put(1L, new MpaRating(1L, "G", "Нет возрастных ограничений"));
        allMpaRatings.put(2L, new MpaRating(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"));
        allMpaRatings.put(3L, new MpaRating(3L, "PG-13", "Детям до 13 лет просмотр не желателен"));
        allMpaRatings.put(4L, new MpaRating(4L, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"));
        allMpaRatings.put(5L, new MpaRating(5L, "NC-17", "Лицам до 18 лет просмотр запрещён"));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values().stream()
                .map(this::enrichFilmWithGenres)
                .collect(Collectors.toList());
    }

    @Override
    public Film create(Film film) {
        film.setId(nextId++);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating fullMpa = allMpaRatings.get(film.getMpa().getId());
            if (fullMpa != null) {
                film.setMpa(new MpaRating(fullMpa.getId(), fullMpa.getName(), fullMpa.getDescription()));
            }
        }

        films.put(film.getId(), film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            filmGenres.put(film.getId(), genreIds);
        }

        return enrichFilmWithGenres(film);
    }

    @Override
    public Film update(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating fullMpa = allMpaRatings.get(film.getMpa().getId());
            if (fullMpa != null) {
                film.setMpa(new MpaRating(fullMpa.getId(), fullMpa.getName(), fullMpa.getDescription()));
            }
        }

        films.put(film.getId(), film);

        filmGenres.remove(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            filmGenres.put(film.getId(), genreIds);
        }

        return enrichFilmWithGenres(film);
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
        likes.remove(id);
        filmGenres.remove(id);
    }

    @Override
    public Optional<Film> findById(Long id) {
        Film film = films.get(id);
        if (film != null) {
            film = enrichFilmWithGenres(film);
        }
        return Optional.ofNullable(film);
    }

    @Override
    public boolean existsById(Long id) {
        return films.containsKey(id);
    }

    private Film enrichFilmWithGenres(Film film) {
        if (film == null) return null;

        Set<Long> genreIds = filmGenres.getOrDefault(film.getId(), new HashSet<>());
        Set<Genre> fullGenres = genreIds.stream()
                .map(id -> {
                    Genre genre = allGenres.get(id);
                    return genre != null ? new Genre(genre.getId(), genre.getName()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Genre::getId))));

        film.setGenres(fullGenres);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Set<Long> filmLikes = likes.computeIfAbsent(filmId, k -> new HashSet<>());
        filmLikes.add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        if (likes.containsKey(filmId)) {
            likes.get(filmId).remove(userId);
        }
    }

    @Override
    public Collection<Long> getFilmLikes(Long filmId) {
        return likes.getOrDefault(filmId, new HashSet<>());
    }

    @Override
    public int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, new HashSet<>()).size();
    }

    @Override
    public void addFilmGenres(Long filmId, Collection<Long> genreIds) {
        Set<Long> currentGenres = filmGenres.computeIfAbsent(filmId, k -> new HashSet<>());
        currentGenres.addAll(genreIds);

        Film film = films.get(filmId);
        if (film != null) {
            enrichFilmWithGenres(film);
        }
    }

    @Override
    public void removeAllFilmGenres(Long filmId) {
        filmGenres.remove(filmId);

        Film film = films.get(filmId);
        if (film != null) {
            film.setGenres(new HashSet<>());
        }
    }

    @Override
    public Collection<Long> getFilmGenreIds(Long filmId) {
        return filmGenres.getOrDefault(filmId, new HashSet<>());
    }
}