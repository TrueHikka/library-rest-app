package ru.library.services.book_service;

import jakarta.transaction.Transactional;
import ru.library.models.Book;

import java.util.List;

public interface BookServiceInf {
    @Transactional
    List<Book> getAllBooks();

    @Transactional
    Book findBookById(Long bookId);

    @Transactional
    void save(Book book);

    @Transactional
    void update(Book book, Long bookId);

    @Transactional
    void softDeleteBook(Long bookId);

    @Transactional
    List<Book> getDeletedBooks();

    @Transactional
    byte[] getCoverImage(Long bookId);

    @Transactional
    List<String> getAllCoverImagesUrl();

    @Transactional
    void assignBookToPerson(Long bookId, Long personId);

    @Transactional
    void freeBook(Long bookId);

    @Transactional
    void viewBookCover(Long bookId, Long personId);

    @Transactional
    void viewBookContent(Long bookId, Long personId);

    @Transactional
    void releaseBookAfterViewing(Long bookId);
}
