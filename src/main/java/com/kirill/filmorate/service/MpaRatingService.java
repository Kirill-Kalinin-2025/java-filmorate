package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.MpaRatingStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MpaRatingService {

    private final MpaRatingStorage mpaRatingStorage;

    @Autowired
    public MpaRatingService(MpaRatingStorage mpaRatingStorage) {
        this.mpaRatingStorage = mpaRatingStorage;
    }

    public Collection<MpaRating> getAllMpaRatings() {
        return mpaRatingStorage.findAll();
    }

    public MpaRating getMpaRatingById(Long id) {
        return mpaRatingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с ID " + id + " не найден"));
    }
}