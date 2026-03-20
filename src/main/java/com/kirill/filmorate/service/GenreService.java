package com.kirill.filmorate.service;

import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.storage.GenreStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }
}