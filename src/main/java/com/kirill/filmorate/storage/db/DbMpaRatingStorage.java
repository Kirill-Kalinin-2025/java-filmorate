package com.kirill.filmorate.storage.db;

import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.MpaRatingStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
public class DbMpaRatingStorage implements MpaRatingStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbMpaRatingStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapMpaRating);
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapMpaRating, id);
        return ratings.stream().findFirst();
    }

    private MpaRating mapMpaRating(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getLong("id"), rs.getString("name"), rs.getString("description"));
    }
}