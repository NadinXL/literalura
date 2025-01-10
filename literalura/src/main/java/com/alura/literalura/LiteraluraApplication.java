package com.alura.literalura;

import com.alura.literalura.main.Main;
import com.alura.literalura.repository.IAuthorRepository;
import com.alura.literalura.repository.IbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner{
    @Autowired
    private IbookRepository bookRepository;
    @Autowired
    private IAuthorRepository authorRepository;
    public static void main(String[] args) {
        SpringApplication.run(LiteraluraApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Main main = new Main(bookRepository, authorRepository);
        main.displayMenu();
    }
}
