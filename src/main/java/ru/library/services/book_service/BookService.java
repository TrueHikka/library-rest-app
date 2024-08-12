package ru.library.services.book_service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.library.dto.BookDTO;
import ru.library.models.Book;
import ru.library.models.BookStatus;
import ru.library.models.Person;
import ru.library.repositories.BookRepository;
import ru.library.repositories.PeopleRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService implements BookServiceInf{
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;
    private final PeopleRepository peopleRepository;

    @Autowired
    public BookService(BookRepository bookRepository, PeopleRepository peopleRepository, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.peopleRepository = peopleRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book findBookById(Long bookId) {
        Optional<Book> bookById = bookRepository.findById(bookId);
        return bookById.orElse(null);
    }

    @Override
    public void save(Book book) {
        bookRepository.save(book);
    }

    @Override
    public byte[] getCoverImage(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            return book.getCoverImage();
        }
        return new byte[0];
    }

    @Override
    public List<String> getAllCoverImagesUrl() {
        List<Book> books = bookRepository.findAll();
        List<String> imageUrls = new ArrayList<>();
        for (Book book : books) {
            byte[] imageData = book.getCoverImage();
            if (imageData != null && imageData.length > 0) {
                String imageUrl = "/api/books/" + book.getBookId() + "/coverImage";
                imageUrls.add(imageUrl);
            }
        }
        return imageUrls;
    }

    @Override
    public void update(Book book, Long bookId) {
        book.setBookId(bookId);
        bookRepository.save(book);
    }

    @Override
    public void softDeleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        book.setRemovedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    @Override
    public List<Book> getDeletedBooks() {
        return bookRepository.findByRemovedAtNotNull();
    }

    public BookDTO convertBookToBookDTO(Book book){
        return modelMapper.map(book, BookDTO.class);

    }

    public Book convertBookDTOToBook(BookDTO bookDTO){
        Book book = modelMapper.map(bookDTO, Book.class);
        enrichBook(book);
        return book;
    }

    private void enrichBook(Book book){
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        book.setRemovedAt(null);
        book.setCreatedPerson("ADMIN");
        book.setUpdatedPerson("ADMIN");
        book.setRemovedPerson(null);
        book.setBookOwner(null);
    }

    @Override
    public void assignBookToPerson(Long bookId, Long personId) {
        Book bookById = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        Person personById  = peopleRepository.findById(personId).orElseThrow(() -> new RuntimeException("Person not found"));

        if (bookById.getStatus() != BookStatus.FREE) {
            throw new IllegalStateException("Book is already assigned to a person");
        }

        bookById.setStatus(BookStatus.ASSIGNED);
        bookById.setBookOwner(personById);
        personById.getBooks().add(bookById);

        bookRepository.save(bookById);
        peopleRepository.save(personById);
    }

    @Override
    public void freeBook(Long bookId) {
        Book bookById = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

        if (bookById.getStatus() != BookStatus.ASSIGNED) {
            throw new IllegalStateException("Book is not assigned to anyone");
        }

        bookById.setStatus(BookStatus.FREE);
        bookById.setBookOwner(null);

        bookRepository.save(bookById);
    }

    @Override
    public void viewBookCover(Long bookId, Long personId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getStatus() != BookStatus.FREE) {
            throw new IllegalStateException("Book is not available for viewing");
        }
        book.setStatus(BookStatus.VIEWING_COVER);
        bookRepository.save(book);
    }

    @Override
    public void viewBookContent(Long bookId, Long personId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getStatus() != BookStatus.FREE) {
            throw new IllegalStateException("Book is not available for viewing");
        }
        book.setStatus(BookStatus.VIEWING_CONTENT);
        bookRepository.save(book);
    }

    @Override
    public void releaseBookAfterViewing(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getStatus() == BookStatus.VIEWING_COVER || book.getStatus() == BookStatus.VIEWING_CONTENT) {
            book.setStatus(BookStatus.FREE);
            bookRepository.save(book);
        }
    }
}
