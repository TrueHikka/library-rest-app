package ru.library.services.general_service;

import jakarta.transaction.Transactional;
import ru.library.models.Book;

import java.util.List;

public interface GeneralBookServiceInf {
    @Transactional
    List<Book> getAllBooks();

    @Transactional
    Book findBookById(Long bookId);

    @Transactional
    void freeBook(Long bookId);

    @Transactional
    byte[] getCoverImage(Long bookId);

    @Transactional
    List<String> getAllCoverImagesUrl();

    @Transactional
    void viewBookCover(Long bookId, Long personId);

    @Transactional
    void viewBookContent(Long bookId, Long personId);

    @Transactional
    void releaseBookAfterViewing(Long bookId);
}
