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
    void delete(Long id);

    @Transactional
    void assignBookToPerson(Long bookId, Long personId);

    @Transactional
    void freeBook(Long bookId);
}
