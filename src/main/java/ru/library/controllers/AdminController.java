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
import ru.library.dto.PersonDTO;
import ru.library.exceptions.book_exp.BookErrorResponse;
import ru.library.exceptions.book_exp.BookNotCreatedException;
import ru.library.exceptions.book_exp.BookNotFoundException;
import ru.library.exceptions.img_exp.ImageErrorResponse;
import ru.library.exceptions.img_exp.MalformedUrlException;
import ru.library.exceptions.person_exp.PersonErrorResponse;
import ru.library.exceptions.person_exp.PersonNotCreatedException;
import ru.library.exceptions.person_exp.PersonNotFoundException;
import ru.library.models.Book;
import ru.library.models.BookStatus;
import ru.library.models.Person;
import ru.library.services.admin_service.AdminService;
import ru.library.util.ImageUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //!People
    @PostMapping("/createNewPerson")
    public ResponseEntity<Person> createNewPerson(@RequestBody @Valid PersonDTO personDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for(FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new PersonNotCreatedException(errors.toString());
        }

        Person person = adminService.convertPersonDTOToPerson(personDTO);

        adminService.save(person);

        return ResponseEntity.ok(person);
    }

    @PutMapping("/{id}/updatePerson")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable("id") Long id, @RequestBody @Valid PersonDTO personDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for(FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new PersonNotCreatedException(errors.toString());
        }

        if (adminService.findPersonById(id) == null) {
            throw new PersonNotFoundException("Person with id " + id + " not found");
        }

        Person person = adminService.convertPersonDTOToPerson(personDTO);

        adminService.update(person, id);

        PersonDTO updatedPersonDTO = adminService.convertPersonToPersonDTO(person);

        return ResponseEntity.ok(updatedPersonDTO);
    }

    @PostMapping("/deletePerson/{id}")
    public ResponseEntity<Void> softDeletePerson(@PathVariable Long id) {
        adminService.softDeletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted/people")
    public ResponseEntity<List<PersonDTO>> getDeletedPeople() {
        List<Person> deletedPeople = adminService.getDeletedPeople();

        return ResponseEntity.ok(deletedPeople.stream().map(adminService::convertPersonToPersonDTO)
                .toList());
    }

    @GetMapping("/personsBook/{id}")
    public ResponseEntity<List<BookDTO>> getBooksByPersonId(@PathVariable("id") Long id) {
        List<BookDTO> booksByPersonId = adminService.getBooksByPersonId(id);
        return ResponseEntity.ok(booksByPersonId);
    }

    //!Books
    @PostMapping("/createNewBook")
    public ResponseEntity<Book> createNewBook(@RequestBody @Valid BookDTO bookDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new BookNotCreatedException(errors.toString());
        }

        Book book = adminService.convertBookDTOToBook(bookDTO);

        try {
            byte[] imageBytes = ImageUtil.downloadImage(bookDTO.getCoverImageURL());
            book.setCoverImage(imageBytes);
        } catch (MalformedUrlException | IOException e) {
            throw new MalformedUrlException(e.getMessage());
        }

        book.setStatus(BookStatus.FREE);

        adminService.save(book);

        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}/updateBook")
    public ResponseEntity<BookDTO> updateBook(@PathVariable("id") Long bookId, @RequestBody @Valid BookDTO bookDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new BookNotCreatedException(errors.toString());
        }

        if (adminService.findBookById(bookId) == null) {
            throw new BookNotFoundException("Book with id " + bookId + " not found");
        }

        Book book = adminService.convertBookDTOToBook(bookDTO);

        adminService.update(book, bookId);

        BookDTO updatedBookDTO = adminService.convertBookToBookDTO(book);

        return ResponseEntity.ok(updatedBookDTO);
    }

    @PostMapping("/deleteBook/{bookId}")
    public ResponseEntity<Void> softDeleteBook(@PathVariable Long bookId) {
        adminService.softDeleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted/books")
    public ResponseEntity<List<BookDTO>> getDeletedBooks() {
        List<Book> deletedBooks = adminService.getDeletedBooks();

        return ResponseEntity.ok(deletedBooks.stream().map(adminService::convertBookToBookDTO)
                .toList());
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<String> assignBook(@PathVariable("id") Long bookId, @RequestParam("personId") Long personId) {
        try {
            adminService.assignBookToPerson(bookId, personId);
            return ResponseEntity.ok("Book " + bookId + " assigned to person " + personId + " successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/free")
    public ResponseEntity<String> freeBook(@PathVariable("id") Long bookId) {
        try {
            adminService.freeBook(bookId);
            return ResponseEntity.ok("Book " + bookId + " free successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{bookId}/coverImage")
    public ResponseEntity<byte[]> getCoverImage(@PathVariable("bookId") Long bookId, @RequestParam("personId") Long personId ) {
        try {
            adminService.viewBookCover(bookId, personId);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage().getBytes());
        }

        Book book = adminService.findBookById(bookId);

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
        List<String> imageUrls = adminService.getAllCoverImagesUrl();
        return ResponseEntity.ok(imageUrls);
    }

    private String getContentType(byte[] imageData) throws MagicMatchNotFoundException, MagicException, MagicParseException {
        MagicMatch match = Magic.getMagicMatch(imageData);
        return match.getMimeType();
    }

    @GetMapping("/{bookId}/content")
    public ResponseEntity<?> getBookContent(@PathVariable("bookId") Long bookId, @RequestParam("personId") Long personId) {
        try {
            adminService.viewBookContent(bookId, personId);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }

        Book book = adminService.findBookById(bookId);

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
            adminService.releaseBookAfterViewing(bookId);
            return ResponseEntity.ok("Book released successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    //!ExceptionHandlers
    @ExceptionHandler({PersonNotFoundException.class})
    public ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException ex) {
        PersonErrorResponse response = new PersonErrorResponse(
                ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({PersonNotCreatedException.class})
    public ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException ex) {
        PersonErrorResponse response = new PersonErrorResponse(
                ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
