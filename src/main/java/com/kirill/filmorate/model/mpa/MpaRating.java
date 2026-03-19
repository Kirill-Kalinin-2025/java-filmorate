package com.kirill.filmorate.model.mpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaRating {
    private Long id;
    private String name;
    private String description;

    public MpaRating(Long id, String name) {
        this.id = id;
        this.name = name;
        this.description = "";
    }

    public static final MpaRating G = new MpaRating(1L, "G", "Нет возрастных ограничений");
    public static final MpaRating PG = new MpaRating(2L, "PG", "Детям рекомендуется смотреть фильм с родителями");
    public static final MpaRating PG_13 = new MpaRating(3L, "PG-13", "Детям до 13 лет просмотр не желателен");
    public static final MpaRating R = new MpaRating(4L, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого");
    public static final MpaRating NC_17 = new MpaRating(5L, "NC-17", "Лицам до 18 лет просмотр запрещён");
}