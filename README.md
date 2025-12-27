# Filmorate

## Схема базы данных

![Диаграмма базы данных Filmorate](database-diagram.png)

### Описание таблиц

**Основные таблицы:**

- `users` - пользователи системы
- `films` - фильмы
- `mpa_ratings` - возрастные рейтинги (G, PG, PG-13, R, NC-17)
- `genres` - жанры фильмов (Комедия, Драма и др.)

**Таблицы связей:**

- `film_genres` - связь фильмов с жанрами (многие-ко-многим)
- `likes` - лайки пользователей фильмам
- `friendships` - дружеские связи между пользователями со статусами (PENDING/CONFIRMED)

### Примеры SQL запросов для основных операций

**1. Получение топ-10 популярных фильмов (по лайкам):**

```sql
SELECT f.*, 
COUNT(l.user_id) as likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;
```

**2. Получение списка фильмов по жанру:**

```sql
SELECT f.name, 
f.description, 
f.release_date, 
f.duration, 
f.mpa_id
FROM films f
INNER JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 1
ORDER BY f.release_date DESC;
```

**3. Получение списка фильмов по жанру и по возрастному рейтингу:**

```sql
SELECT f.name, 
f.description, 
f.release_date, 
f.duration, 
f.mpa_id
FROM films f
INNER JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 1 AND f.mpa_id = 1
ORDER BY f.release_date DESC;
```
## Полная схема базы данных для приложения Filmorate

### ПОЛЬЗОВАТЕЛИ 
### Таблица для хранения информации о пользователях
users
```sql
id          BIGINT          PRIMARY KEY, AUTO_INCREMENT   # Первичный ключ  
email       VARCHAR(255)    UNIQUE, NOT NULL              # Уникальный email  
login       VARCHAR(50)     UNIQUE, NOT NULL              # Логин (без пробелов)  
name        VARCHAR(100)                                  # Имя для отображения  
birthday    DATE            NOT NULL                      # Дата рождения  
```
### РЕЙТИНГИ MPA
### Справочная таблица возрастных рейтингов
mpa_ratings
```sql
id          BIGINT          PRIMARY KEY                    # Первичный ключ  
name        VARCHAR(10)     UNIQUE, NOT NULL               # Код: G, PG, PG-13, R, NC-17  
description VARCHAR(255)                                   # Описание рейтинга  
```
### ЖАНРЫ
### Справочная таблица жанров фильмов
genres
```sql
id          BIGINT          PRIMARY KEY                    # Первичный ключ  
name        VARCHAR(50)     UNIQUE, NOT NULL               # Название жанра  
```

### ФИЛЬМЫ
### Основная таблица для хранения информации о фильмах
films
```sql
id          BIGINT          PRIMARY KEY, AUTO_INCREMENT    # Первичный ключ  
name        VARCHAR(200)    NOT NULL                       # Название фильма  
description VARCHAR(200)                                   # Описание (макс. 200 симв.)  
release_date DATE           NOT NULL                       # Дата выхода  
duration    INTEGER         NOT NULL                       # Продолжительность (мин.)  
mpa_id      BIGINT          FOREIGN KEY → mpa_ratings(id)  # Ссылка на рейтинг MPA  
```
### СВЯЗЬ ФИЛЬМЫ-ЖАНРЫ
### Связь многие-ко-многим между фильмами и жанрами
film_genres
```sql
film_id     BIGINT          FOREIGN KEY → films(id)        # Ссылка на фильм  
genre_id    BIGINT          FOREIGN KEY → genres(id)       # Ссылка на жанр  
## Составной первичный ключ: (film_id, genre_id)
```

### ЛАЙКИ
### Связь многие-ко-многим для лайков пользователей
likes
```sql
film_id     BIGINT          FOREIGN KEY → films(id)        # Ссылка на фильм   
user_id     BIGINT          FOREIGN KEY → users(id)        # Ссылка на пользователя   
created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP      # Дата создания лайка   
## Составной первичный ключ: (film_id, user_id)
```
### ДРУЖБА
### Связь многие-ко-многим для дружбы между пользователями
friendships
```sql
user_id     BIGINT          FOREIGN KEY → users(id)        # Ссылка на пользователя  
friend_id   BIGINT          FOREIGN KEY → users(id)        # Ссылка на друга  
status      VARCHAR(10)     DEFAULT 'PENDING'              # PENDING или CONFIRMED  
created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP      # Дата создания  
updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP      # Дата обновления  
## Составной первичный ключ: (user_id, friend_id)
```