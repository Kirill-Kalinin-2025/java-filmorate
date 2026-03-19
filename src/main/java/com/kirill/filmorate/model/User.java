package com.kirill.filmorate.model;

import com.kirill.filmorate.model.friendship.FriendshipStatus;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Map;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректного формата")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Map<Long, FriendshipStatus> friendshipStatuses;
}