package com.kirill.filmorate.model.genre;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    private Long id;
    private String name;

    public static final Genre COMEDY = new Genre(1L, "Комедия");
    public static final Genre DRAMA = new Genre(2L, "Драма");
    public static final Genre CARTOON = new Genre(3L, "Мультфильм");
    public static final Genre THRILLER = new Genre(4L, "Триллер");
    public static final Genre DOCUMENTARY = new Genre(5L, "Документальный");
    public static final Genre ACTION = new Genre(6L, "Боевик");
}