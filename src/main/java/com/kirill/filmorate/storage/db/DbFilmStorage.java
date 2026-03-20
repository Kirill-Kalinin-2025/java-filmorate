package com.kirill.filmorate.storage.db;

import com.kirill.filmorate.exception.NotFoundException;
import com.kirill.filmorate.model.Film;
import com.kirill.filmorate.model.genre.Genre;
import com.kirill.filmorate.model.mpa.MpaRating;
import com.kirill.filmorate.storage.FilmStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, mr.name as mpa_name, mr.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapFilm(rs));

        loadGenresForFilms(films);

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, mr.name as mpa_name, mr.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id " +
                "WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapFilm(rs), id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenresForFilms(Collections.singletonList(film));

        return Optional.of(film);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenres(id, film.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        }

        loadGenresForFilms(Collections.singletonList(film));

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        removeAllFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenres(film.getId(), film.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        }

        loadGenresForFilms(Collections.singletonList(film));

        return film;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Long> getFilmLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId);
    }

    @Override
    public void addFilmGenres(Long filmId, Collection<Long> genreIds) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Long genreId : genreIds) {
            batchArgs.add(new Object[]{filmId, genreId});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void removeAllFilmGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public Collection<Long> getFilmGenreIds(Long filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
    }

    @Override
    public Collection<Film> findPopularFilms(int count) {
        String sql = "SELECT f.*, mr.name as mpa_name, mr.description as mpa_description, " +
                "COUNT(l.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, mr.name, mr.description " +
                "ORDER BY likes_count DESC, f.id " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapFilm(rs), count);

        loadGenresForFilms(films);

        return films;
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY g.id";

        Map<Long, Set<Genre>> genresMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getLong("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new TreeSet<>(Comparator.comparing(Genre::getId))).add(genre);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new TreeSet<>(Comparator.comparing(Genre::getId))));
        }
    }

    private Film mapFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        mpa.setDescription(rs.getString("mpa_description"));
        film.setMpa(mpa);

        return film;
    }
}