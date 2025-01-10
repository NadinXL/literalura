package com.alura.literalura.main;
import com.alura.literalura.dto.AuthorDTO;
import com.alura.literalura.dto.JsonDTO;
import com.alura.literalura.entity.Author;
import com.alura.literalura.entity.Book;
import com.alura.literalura.repository.IAuthorRepository;
import com.alura.literalura.repository.IbookRepository;
import com.alura.literalura.service.ConnectionAPI;
import com.alura.literalura.service.DataConvertion;
import org.springframework.dao.DataIntegrityViolationException;


import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private ConnectionAPI cnx = new ConnectionAPI();
    private DataConvertion dataConvertion = new DataConvertion();
    private static final String API_URL = "https://gutendex.com/books/";

    private IbookRepository bookRepository;
    private IAuthorRepository authorRepository;

    public Main(IbookRepository bookRepository, IAuthorRepository authorRepository){
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    private int opc = -1;

    public void displayMenu(){
        while(opc != 0){
            System.out.println("---------- Menú ---------- ");
            System.out.println("""
                    1. Buscar libro por título
                    2. Listar libros registrados
                    3. Buscar autor por nombre
                    4. Listar autores registrados
                    5. Listar autores vivos en un determinado año
                    6. Listar libros por idioma
                    7. Top 10 libros más descargados
                    0. Salir
                    """);

            try{
                opc = Integer.parseInt(sc.nextLine());

                switch(opc){
                    case 1:
                        getBook();
                        break;
                    case 2:
                        System.out.println("----- Libros Registrados -----\n");
                        getAllListedBooks();
                        break;
                    case 3:
                        getAuthorByName();
                        break;
                    case 4:
                        System.out.println("----- Autores Registrados -----\n");
                        getListedAuthors();
                        break;
                    case 5:
                        getAuthorBetweenYears();
                        break;
                    case 6:
                        getBooksByLanguage();
                        break;
                    case 7:
                        System.out.println("----- Top 10 libros más descargados -----\n");
                        getTop10Books();
                        break;
                    case 0:
                        System.out.println("Gracias por usar LiterAlura!\n");
                        break;
                    default:
                        System.out.println("Opción elegida incorrecta. Elija nuevamente.\n");
                }
            }catch(NumberFormatException e){
                System.out.println("Debes seleccionar un número.");
            }
        }
    }

    private void getBook() {
        System.out.println("Escriba el nombre del libro: ");
        String bookName = sc.nextLine();

        String json = cnx.getData(API_URL + "?search=" + bookName.replace(" ", "+"));
        JsonDTO results = dataConvertion.convertData(json, JsonDTO.class);

        Optional<Book> books = results.bookResults().stream()
                .findFirst()
                .map(b -> new Book(b));

        if (books.isPresent()) {
            Book book = books.get();

            if (book.getAuthor() != null) {
                Author author = authorRepository.findAuthorsByName(book.getAuthor().getName());

                if (author == null) {
                    // Crear y guardar un nuevo autor si no existe
                    Author newAuthor = book.getAuthor();
                    author = authorRepository.save(newAuthor);
                }

                try {
                    // Asociar el autor existente con el libro
                    book.setAuthor(author);
                    bookRepository.save(book);
                    System.out.println(book);
                } catch (DataIntegrityViolationException e) {
                    System.out.println("El libro ya se encuentra registrado en la base de datos.");
                }
            }
        } else {
            System.out.println("No se encontró el libro: " + bookName);
        }
    }
    private void getAllListedBooks(){
        List<Book> books = bookRepository.findAll();
        books.forEach(System.out::println);
    }

    private void getAuthorByName(){
        System.out.println("Escribe el nombre del autor que deseas buscar: ");
        String authorName = sc.nextLine();

        if(isNumber(authorName)){
            System.out.println("Debes ingresar un nombre, no un número.");
        }else{
            String json = cnx.getData(API_URL + "?search=" + authorName.replace(" ", "+"));
            JsonDTO results = dataConvertion.convertData(json, JsonDTO.class);

            Optional<AuthorDTO> author = results.bookResults().stream()
                    .findFirst()
                    .map(a -> new AuthorDTO(a.authors().get(0).authorName(), a.authors().get(0).birthYear(), a.authors().get(0).deathYear()));

            if(author.isPresent()){
                System.out.println(author.get());
            }else{
                System.out.println("No se encontró autor con el nombre: " + authorName);
            }
        }
    }

    private boolean isNumber(String authorName) {
        try {
            Double.parseDouble(authorName);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void getListedAuthors(){
        List<Author> authors = authorRepository.findAll();
        authors.forEach(System.out::println);
    }

    private void getAuthorBetweenYears(){
        System.out.println("Ingrese el año vivo del autor(es) que desea buscar: ");
        try{
            int year = sc.nextInt();
            List<Author> authors = authorRepository.findAuthorBetweenYear(year);
            if(authors.isEmpty()){
                System.out.println("No se encontraron registros de autores vivos durante ese año en la base de datos.");
            }else{
                authors.forEach(System.out::println);
            }

        }catch (InputMismatchException e){
            System.out.println("Debes ingresar un año válido.");
        }
        sc.nextLine();
    }

    private void getBooksByLanguage(){
        System.out.println("Ingrese el idioma que desea buscar: ");
        System.out.println("""
                es -> Español
                en -> Inglés
                fr -> Francés
                pt -> Portugés
                """);

        String language = sc.nextLine();

        List<Book> books = bookRepository.findBookByLanguage(language.toUpperCase());
        if(books.isEmpty()){
            System.out.println("No se encontraron libros en ese idioma");
        }else{
            books.forEach(System.out::println);
        }
    }

    private void getTop10Books(){
        String json = cnx.getData(API_URL);
        JsonDTO results = dataConvertion.convertData(json, JsonDTO.class);

        List<Book> top10Books = results.bookResults().stream()
                .map(b -> new Book(b))
                .sorted(Comparator.comparingLong(Book::getDownloads_count).reversed())
                .limit(10)
                .collect(Collectors.toList());

        top10Books.stream()
                .forEach(b -> System.out.println(b.getTitle() + " : (" + b.getDownloads_count() + " descargas)\n"));
    }
}
