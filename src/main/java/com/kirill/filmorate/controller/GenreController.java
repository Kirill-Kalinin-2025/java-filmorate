package com.kirill.filmorate.controller;

import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.service.GenreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public Collection<Genre> getAllGenres() {
        log.info("Получен запрос на получение всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Long id) {
        log.info("Получен запрос на получение жанра с ID: {}", id);
        return genreService.getGenreById(id);
    }
}