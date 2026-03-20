package com.kirill.filmorate.storage;

import com.kirill.filmorate.model.mpa.MpaRating;

import java.util.Collection;
import java.util.Optional;

public interface MpaRatingStorage {
    Collection<MpaRating> findAll();

    Optional<MpaRating> findById(Long id);
}