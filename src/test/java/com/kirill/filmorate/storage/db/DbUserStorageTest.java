package com.kirill.filmorate.storage.db;

import com.kirill.filmorate.model.User;
import com.kirill.filmorate.storage.UserStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(DbUserStorage.class)
@ActiveProfiles("test")
class DbUserStorageTest {

    @Autowired
    private UserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateUser() {
        User created = userStorage.create(testUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(created.getLogin()).isEqualTo(testUser.getLogin());
    }

    @Test
    void shouldFindUserById() {
        User created = userStorage.create(testUser);

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        User created = userStorage.create(testUser);
        created.setName("Обновленное имя");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Обновленное имя");
    }

    @Test
    void shouldDeleteUser() {
        User created = userStorage.create(testUser);

        userStorage.delete(created.getId());

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(testUser);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("another");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1995, 1, 1));
        userStorage.create(anotherUser);

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldCheckEmailExists() {
        userStorage.create(testUser);

        boolean exists = userStorage.existsEmail(testUser.getEmail(), null);
        assertThat(exists).isTrue();

        boolean notExists = userStorage.existsEmail("nonexistent@example.com", null);
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldAddAndConfirmFriend() {
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@example.com");
        user2.setLogin("friend");
        user2.setName("Friend User");
        user2.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser2 = userStorage.create(user2);

        // Добавляем друга
        userStorage.addFriend(user1.getId(), createdUser2.getId());

        Collection<Long> friends = userStorage.getUserFriendIds(user1.getId());
        assertThat(friends).contains(createdUser2.getId());

        // Подтверждаем дружбу
        userStorage.confirmFriend(user1.getId(), createdUser2.getId());

        // Удаляем друга
        userStorage.removeFriend(user1.getId(), createdUser2.getId());

        Collection<Long> friendsAfterDelete = userStorage.getUserFriendIds(user1.getId());
        assertThat(friendsAfterDelete).doesNotContain(createdUser2.getId());
    }
}