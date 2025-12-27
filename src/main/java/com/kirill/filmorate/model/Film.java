package com.kirill.filmorate.model;

import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность обязательна")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    @NotNull(message = "MPA рейтинг обязателен")
    private MpaRating mpa;

    @NotNull(message = "Список жанров не может быть null")
    private Set<Genre> genres = new HashSet<>();

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return false;
        }
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return releaseDate.isAfter(minDate) || releaseDate.isEqual(minDate);
    }

    @AssertTrue(message = "Список жанров не может содержать повторяющиеся жанры")
    public boolean isGenresUnique() {
        if (genres == null) return true;
        return genres.stream()
                .map(Genre::getId)
                .distinct()
                .count() == genres.size();
    }

    @AssertTrue(message = "Фильм не может иметь более 5 жанров")
    public boolean isGenresSizeValid() {
        if (genres == null) return true;
        return genres.size() <= 5;
    }

    @AssertTrue(message = "Жанры должны иметь валидные ID")
    public boolean areGenresValid() {
        if (genres == null || genres.isEmpty()) return true;
        return genres.stream()
                .allMatch(genre -> genre != null && genre.getId() != null && genre.getId() > 0);
    }
}