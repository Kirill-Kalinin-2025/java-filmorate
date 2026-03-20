package com.kirill.filmorate.storage.db;

import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.storage.GenreStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
public class DbGenreStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, this::mapGenre);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapGenre, id);
        return genres.stream().findFirst();
    }

    @Override
    public Collection<Genre> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM genres WHERE id IN (" + placeholders + ") ORDER BY id";

        return jdbcTemplate.query(sql, this::mapGenre, ids.toArray());
    }

    private Genre mapGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("id"), rs.getString("name"));
    }
}