package com.kirill.filmorate.controller;

import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.service.MpaRatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaRatingService mpaRatingService;

    @Autowired
    public MpaController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping
    public Collection<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех MPA рейтингов");
        return mpaRatingService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRatingById(@PathVariable Long id) {
        log.info("Получен запрос на получение MPA рейтинга с ID: {}", id);
        return mpaRatingService.getMpaRatingById(id);
    }
}