import com.fasterxml.jackson.databind.ObjectMapper;
import model.Film;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Матрица");
        validFilm.setDescription("Фильм про реальность");
        validFilm.setReleaseDate(LocalDate.of(1999, 3, 28));
        validFilm.setDuration(136);
    }

    @Test
    void createFilm_ValidFilm_ReturnsCreatedFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Матрица")))
                .andExpect(jsonPath("$.duration", is(136)));
    }

    @Test
    void createFilm_EmptyName_ReturnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setDescription("Тест");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_TooLongDescription_ReturnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Тестовый фильм");
        invalidFilm.setDescription("A".repeat(201)); // 201 символов
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Тестовый фильм");
        invalidFilm.setDescription("Тест");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(-1);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_TooEarlyReleaseDate_ReturnsBadRequest() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Тестовый фильм");
        invalidFilm.setDescription("Тест");
        invalidFilm.setReleaseDate(LocalDate.of(1859, 12, 1));
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllFilms_ReturnsEmptyListInitially() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllFilms_ReturnsFilmsAfterCreation() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Матрица")));
    }

    @Test
    void updateFilm_ValidUpdate_ReturnsUpdatedFilm() throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Новая матрица");
        updatedFilm.setDescription("Новое описание");
        updatedFilm.setReleaseDate(LocalDate.of(1999, 3, 28));
        updatedFilm.setDuration(150);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Новая матрица")))
                .andExpect(jsonPath("$.duration", is(150)));
    }

    @Test
    void updateFilm_NonExistentId_ReturnsBadRequest() throws Exception {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Несуществующий");
        nonExistentFilm.setReleaseDate(LocalDate.now());
        nonExistentFilm.setDuration(120);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithoutId_ReturnsBadRequest() throws Exception {
        Film filmWithoutId = new Film();
        filmWithoutId.setName("Без ID фильма");
        filmWithoutId.setReleaseDate(LocalDate.now());
        filmWithoutId.setDuration(120);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithoutId)))
                .andExpect(status().isBadRequest());
    }
}