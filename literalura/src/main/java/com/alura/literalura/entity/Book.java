package com.alura.literalura.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.alura.literalura.dto.BookDTO;

@Data
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book")
    private Long id;
    @Column(unique = true)
    private String title;

    private String language;

    @ManyToOne
    @JoinColumn(name = "id_author")
    private Author author;

    private Long downloads_count;

    public Book(BookDTO bookDTO) {
        this.title = bookDTO.title();
        this.language = bookDTO.languages().get(0).toUpperCase();
        this.author = new Author(bookDTO.authors().get(0));
        this.downloads_count = bookDTO.downloads();
    }

    @Override
    public String toString() {
        return "----- Libro -----" +
                "\n Titulo: " + title +
                "\n Autor: " + author.getName() +
                "\n Idioma: " + language +
                "\n Número de descargas: " + downloads_count +
                "\n-----------------\n";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Long getDownloads_count() {
        return downloads_count;
    }

    public void setDownloads_count(Long downloads_count) {
        this.downloads_count = downloads_count;
    }
}
