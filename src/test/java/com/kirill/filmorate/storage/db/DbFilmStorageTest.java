package com.kirill.filmorate.storage.db;

import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.FilmStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({DbFilmStorage.class, DbGenreStorage.class, DbMpaRatingStorage.class})
@ActiveProfiles("test")
class DbFilmStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Тестовый фильм");
        testFilm.setDescription("Описание тестового фильма");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        testFilm.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        Genre genre1 = new Genre(1L, "Комедия");
        Genre genre2 = new Genre(2L, "Драма");
        genres.add(genre1);
        genres.add(genre2);
        testFilm.setGenres(genres);
    }

    @Test
    void shouldCreateFilm() {
        Film created = filmStorage.create(testFilm);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(testFilm.getName());
    }

    @Test
    void shouldFindFilmById() {
        Film created = filmStorage.create(testFilm);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(testFilm.getName());
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmStorage.create(testFilm);
        created.setName("Обновленное название");

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Обновленное название");
    }

    @Test
    void shouldDeleteFilm() {
        Film created = filmStorage.create(testFilm);

        filmStorage.delete(created.getId());

        Optional<Film> found = filmStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(testFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Еще фильм");
        anotherFilm.setDescription("Описание");
        anotherFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        anotherFilm.setDuration(90);

        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        anotherFilm.setMpa(mpa);

        filmStorage.create(anotherFilm);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }
}