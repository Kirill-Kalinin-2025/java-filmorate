import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;
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

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("kirya.kalina.06@yandex.ru");
        validUser.setLogin("kirill");
        validUser.setName("Кирилл");
        validUser.setBirthday(LocalDate.of(1999, 3, 28));
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("user@mail.com")))
                .andExpect(jsonPath("$.login", is("userlogin")));
    }

    @Test
    void createUser_WithoutName_UsesLoginAsName() throws Exception {
        User userWithoutName = new User();
        userWithoutName.setEmail("kirya.kalina.06@yandex.ru");
        userWithoutName.setLogin("kirill");
        userWithoutName.setBirthday(LocalDate.of(1999, 3, 28));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithoutName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testlogin")));
    }

    @Test
    void createUser_EmptyEmail_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setEmail("");
        invalidUser.setLogin("kirill");
        invalidUser.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setEmail("invalid-email");
        invalidUser.setLogin("kirill");
        invalidUser.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_LoginWithSpaces_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setEmail("kirya.kalina.06@yandex.ru");
        invalidUser.setLogin("login with spaces");
        invalidUser.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_FutureBirthday_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setEmail("kirya.kalina.06@yandex.ru");
        invalidUser.setLogin("kirill");
        invalidUser.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_ReturnsUsersList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() throws Exception {
        // Создаем пользователя
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);

        // Обновляем
        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("updated@mail.com");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(2000, 3, 28));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@mail.com")))
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void updateUser_NonExistentId_ReturnsBadRequest() throws Exception {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("kirya.kalina.06@yandex.ru");
        nonExistentUser.setLogin("kirill");

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isBadRequest());
    }
}