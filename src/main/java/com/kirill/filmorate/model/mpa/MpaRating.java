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
}