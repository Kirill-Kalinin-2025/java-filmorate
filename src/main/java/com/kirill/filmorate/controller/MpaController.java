package com.kirill.filmorate.controller;

import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.service.FilmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final FilmService filmService;

    @Autowired
    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех MPA рейтингов");
        return filmService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRatingById(@PathVariable Long id) {
        log.info("Получен запрос на получение MPA рейтинга с ID: {}", id);
        return filmService.getMpaRatingById(id);
    }
}