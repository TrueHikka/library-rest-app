package ru.library.controllers;

import jakarta.validation.Valid;
import net.sf.jmimemagic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.library.dto.BookDTO;
import ru.library.exceptions.book_exp.BookErrorResponse;
import ru.library.exceptions.book_exp.BookNotCreatedException;
import ru.library.exceptions.book_exp.BookNotFoundException;
import ru.library.exceptions.img_exp.ImageErrorResponse;
import ru.library.exceptions.img_exp.MalformedUrlException;
import ru.library.exceptions.person_exp.PersonNotFoundException;
import ru.library.models.Book;
import ru.library.models.BookStatus;
import ru.library.services.book_service.BookService;
import ru.library.services.book_service.BookServiceInf;
import ru.library.util.ImageUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/books")
public class BookController {
    private final BookServiceInf bookServiceInf;
    private final BookService bookService;
//    private final PeopleServiceInf peopleServiceinf;

    @Autowired
    public BookController(BookServiceInf bookServiceInf, BookService bookService) {
        this.bookServiceInf = bookServiceInf;
        this.bookService = bookService;
    }

    @GetMapping()
    public List<BookDTO> getAllBooks() {
        List<Book> allBooks = bookServiceInf.getAllBooks();

        return allBooks.stream()
                .map(bookService::convertBookToBookDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public BookDTO getBookById(@PathVariable("id") Long bookId) {
        Book bookById = bookServiceInf.findBookById(bookId);

        return bookService.convertBookToBookDTO(bookById);

    }

    @PostMapping()
    public ResponseEntity<Book> createNewBook(@RequestBody @Valid BookDTO bookDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new BookNotCreatedException(errors.toString());
        }

        Book book = bookService.convertBookDTOToBook(bookDTO);

        try {
            byte[] imageBytes = ImageUtil.downloadImage(bookDTO.getCoverImageURL());
            book.setCoverImage(imageBytes);
        } catch (MalformedUrlException | IOException e) {
            throw new MalformedUrlException(e.getMessage());
        }

        book.setStatus(BookStatus.FREE);

        bookService.save(book);

        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable("id") Long bookId, @RequestBody @Valid BookDTO bookDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new BookNotCreatedException(errors.toString());
        }

        if (bookServiceInf.findBookById(bookId) == null) {
            throw new BookNotFoundException("Book with id " + bookId + " not found");
        }

        Book book = bookService.convertBookDTOToBook(bookDTO);

        bookService.update(book, bookId);

        BookDTO updatedBookDTO = bookService.convertBookToBookDTO(book);

        return ResponseEntity.ok(updatedBookDTO);
    }

    @GetMapping("/{bookId}/coverImage")
    public ResponseEntity<byte[]> getCoverImage(@PathVariable("bookId") Long bookId, @RequestParam("personId") Long personId ) {
        try {
            bookServiceInf.viewBookCover(bookId, personId);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage().getBytes());
        }

        Book book = bookService.findBookById(bookId);

        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageData = book.getCoverImage();

        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        try {
            String contentType = getContentType(imageData);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(contentType));
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (MagicMatchNotFoundException | MagicException | MagicParseException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/coverImages")
    public ResponseEntity<List<String>> getAllCoverImageUrls() {
        List<String> imageUrls = bookService.getAllCoverImagesUrl();
        return ResponseEntity.ok(imageUrls);
    }

    private String getContentType(byte[] imageData) throws MagicMatchNotFoundException, MagicException, MagicParseException {
        MagicMatch match = Magic.getMagicMatch(imageData);
        return match.getMimeType();
    }

    @GetMapping("/{bookId}/content")
    public ResponseEntity<?> getBookContent(@PathVariable("bookId") Long bookId, @RequestParam("personId") Long personId) {
        try {
            bookServiceInf.viewBookContent(bookId, personId);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }

        Book book = bookServiceInf.findBookById(bookId);

        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        BookDTO bookDTO = new BookDTO(
                book.getTitle(),
                book.getYearOfProduction(),
                book.getAuthor(),
                book.getAnnotation(),
                null
        );

        return ResponseEntity.ok(bookDTO);
    }

    @PutMapping("/{bookId}/releaseAfterViewing")
    public ResponseEntity<String> releaseBookAfterViewing(@PathVariable Long bookId) {
        try {
            bookServiceInf.releaseBookAfterViewing(bookId);
            return ResponseEntity.ok("Book released successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<String> assignBook(@PathVariable("id") Long bookId, @RequestParam("personId") Long personId) {
        try {
            bookServiceInf.assignBookToPerson(bookId, personId);
            return ResponseEntity.ok("Book " + bookId + " assigned to person " + personId + " successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/free")
    public ResponseEntity<String> freeBook(@PathVariable("id") Long bookId) {
        try {
            bookServiceInf.freeBook(bookId);
            return ResponseEntity.ok("Book " + bookId + " free successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/delete/{bookId}")
    public ResponseEntity<Void> softDeleteBook(@PathVariable Long bookId) {
        bookService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<BookDTO>> getDeletedBooks() {
        List<Book> deletedBooks = bookServiceInf.getDeletedBooks();

        return ResponseEntity.ok(deletedBooks.stream().map(bookService::convertBookToBookDTO)
                .toList());
    }

    @ExceptionHandler({BookNotCreatedException.class})
    public ResponseEntity<BookErrorResponse> handleBookNotCreatedException(BookNotCreatedException ex) {
        BookErrorResponse errorResponse = new BookErrorResponse(
                ex.getMessage(),
                new Date()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BookNotFoundException.class})
    public ResponseEntity<BookErrorResponse> handleException(BookNotFoundException ex) {
        BookErrorResponse response = new BookErrorResponse(
                ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({MalformedURLException.class})
    public ResponseEntity<ImageErrorResponse> handleMalformedURLException(MalformedURLException ex) {
        ImageErrorResponse response = new ImageErrorResponse(
                "Invalid URL: " + ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
