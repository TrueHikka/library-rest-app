package ru.library.controllers;

import net.sf.jmimemagic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.library.dto.BookDTO;
import ru.library.exceptions.book_exp.BookErrorResponse;
import ru.library.exceptions.book_exp.BookNotCreatedException;
import ru.library.exceptions.book_exp.BookNotFoundException;
import ru.library.exceptions.img_exp.ImageErrorResponse;
import ru.library.models.Book;
import ru.library.services.book_service.BookService;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping()
    public List<BookDTO> getAllBooks() {
        List<Book> allBooks = bookService.getAllBooks();

        return allBooks.stream()
                .map(bookService::convertBookToBookDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public BookDTO getBookById(@PathVariable("id") Long bookId) {
        Book bookById = bookService.findBookById(bookId);

        return bookService.convertBookToBookDTO(bookById);

    }

    @GetMapping("/{bookId}/coverImage")
    public ResponseEntity<byte[]> getCoverImage(@PathVariable("bookId") Long bookId, @RequestParam("personId") Long personId ) {
        try {
            bookService.viewBookCover(bookId, personId);
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
            bookService.viewBookContent(bookId, personId);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }

        Book book = bookService.findBookById(bookId);

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
            bookService.releaseBookAfterViewing(bookId);
            return ResponseEntity.ok("Book released successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/free")
    public ResponseEntity<String> freeBook(@PathVariable("id") Long bookId) {
        try {
            bookService.freeBook(bookId);
            return ResponseEntity.ok("Book " + bookId + " free successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
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
