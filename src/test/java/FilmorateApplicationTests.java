// CHECKSTYLE:OFF
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void filmLifecycleTest() throws Exception {
        String filmJson = """
                 {\s
                 "name": "Кто я",
                     "description": "Бенджамин – молодой компьютерный гений. Но в реальном мире он - никто.",
                     "releaseDate": "2014-09-06",
                     "duration": 102
                 }
                \s""";

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(filmJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void userLifecycleTest() throws Exception {
        String userJson = """
                 {\s
                 "email": "kirya.kalina.06@yandex.ru",
                     "login": "kirill",
                     "birthday": "1999-03-28"
                 }
                \s""";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("leo"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
// CHECKSTYLE:ON