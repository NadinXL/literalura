# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LiterAlura is a console-based book catalog application (an Alura challenge project). It queries the public [Gutendex API](https://gutendex.com/books/) (Project Gutenberg), persists books and authors in PostgreSQL, and exposes an interactive text menu. All user-facing strings are in Spanish.

## Repository Layout

The Maven project lives in the `literalura/` subdirectory, **not** the repo root. Run all build/run/test commands from `literalura/`.

```
literalura/            <- Maven project root (pom.xml, mvnw live here)
  src/main/java/com/alura/literalura/
```

## Commands

Run from `literalura/`:

```bash
./mvnw spring-boot:run          # Run the interactive console app
./mvnw clean package            # Build the jar (target/literalura-0.0.1-SNAPSHOT.jar)
./mvnw test                     # Run all tests
./mvnw test -Dtest=LiteraluraApplicationTests#contextLoads   # Run a single test
```

The app needs a running PostgreSQL instance and three environment variables before it will start (Spring fails fast at context load if the datasource is unreachable):

```bash
export DB_HOST=localhost:5432
export DB_USER=postgres
export DB_PASSWORD=yourpassword
```

The database `literalura` must exist; tables are created/updated automatically via `spring.jpa.hibernate.ddl-auto=update`.

## Architecture

- **Java 17, Spring Boot 3.4.1.** Dependencies: Spring Data JPA, PostgreSQL driver, Jackson (`jackson-databind`), Lombok, DevTools.
- **Entry point (`LiteraluraApplication`)** implements `CommandLineRunner`. Spring injects the two JPA repositories, then it manually constructs `Main` and calls `displayMenu()`. There is no web layer — this is a CLI, not a REST service.
- **`main/Main`** holds the entire menu loop and all use-case logic (search, list, filter). It is a plain object, **not a Spring bean**, so `ConnectionAPI` and `DataConvertion` are `new`-ed directly inside it rather than injected. If you add a collaborator that needs injection, you must thread it through the `Main` constructor from `LiteraluraApplication`.

### Data flow for API-backed operations

`Main` → `ConnectionAPI.getData(url)` (Java `HttpClient`, returns raw JSON string) → `DataConvertion.convertData(json, Class)` (Jackson `ObjectMapper`) → `JsonDTO` → mapped into `Book`/`Author` entities → saved via repositories.

- The API URL is hardcoded: `https://gutendex.com/books/`, searched with `?search=<terms+joined+by+plus>`.
- **DTOs** (`dto/`) are Java records mirroring the Gutendex JSON, using `@JsonAlias` to bind snake_case API fields (`download_count`, `birth_year`) to camelCase record components, and `@JsonIgnoreProperties(ignoreUnknown = true)`. Entities are constructed from DTOs via constructors (`new Book(bookDTO)`, `new Author(authorDTO)`).

### Persistence

- **Entities** (`entity/`): `Book` and `Author` in a `@ManyToOne`/`@OneToMany` relationship (a book has one author; an author has many books, `fetch = FetchType.EAGER`, `cascade = ALL`). Both use Lombok `@Data`/`@NoArgsConstructor` but also hand-write getters/setters and custom `toString()` (the `toString` outputs are the formatted console display).
- Both `Book.title` and `Author.name` are `@Column(unique = true)`. `getBook()` in `Main` relies on this: it looks up an existing author by name before saving and catches `DataIntegrityViolationException` when a duplicate book is inserted.
- **`Book.language` is stored uppercase** (e.g. `EN`, `ES`). The language-filter menu option uppercases user input before querying — preserve this convention in any new language logic.
- **Repositories** (`repository/`) extend `JpaRepository`. Custom finders: `findBookByLanguage` and `findAuthorsByName` use Spring Data derived queries; `findAuthorBetweenYear` uses a native SQL `@Query` (`birth_year`/`death_year` column names) to find authors alive in a given year.

### Menu options (in `Main.displayMenu`)

1. Search book by title (hits API, persists result) · 2. List stored books · 3. Search author by name (API only, not persisted) · 4. List stored authors · 5. List authors alive in a given year · 6. List books by language · 7. Top 10 most-downloaded (from a fresh API call, not the DB) · 0. Exit.

## Conventions & Gotchas

- Naming is inconsistent by convention: the repository interface is `IbookRepository` (lowercase `b`) while `IAuthorRepository` is capitalized. Match the existing file name when referencing them.
- `application.properties` sets `hibernate.dialect=org.hibernate.dialect.HSQLDialect`, but the actual runtime datasource is PostgreSQL; this line is inert (Spring Boot auto-detects the dialect from the driver). Don't treat it as a signal that HSQL is used.
- Network/JSON failures are wrapped in `RuntimeException` and propagate; there is no retry or offline handling.
