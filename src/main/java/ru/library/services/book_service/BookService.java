package ru.library.services.book_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.library.models.Book;
import ru.library.models.Person;
import ru.library.repositories.BookRepository;
import ru.library.repositories.PeopleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BookService implements BookServiceInf{
    private final BookRepository bookRepository;
    private final PeopleRepository peopleRepository;

    @Autowired
    public BookService(BookRepository bookRepository, PeopleRepository peopleRepository) {
        this.bookRepository = bookRepository;
        this.peopleRepository = peopleRepository;
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
    public void update(Book book, Long bookId) {
        book.setBookId(bookId);
        bookRepository.save(book);
    }

    @Override
    public void delete(Long bookId) {
        bookRepository.deleteById(bookId);
    }

    @Override
    public void assignBookToPerson(Long bookId, Long personId) {
        Optional<Book> bookById = bookRepository.findById(bookId);
        Optional<Person> personById  = peopleRepository.findById(personId);

        if (bookById.isPresent() && personById.isPresent()) {
            Book book = bookById.get();
            Person person = personById.get();
            book.setBookOwner(person);
            bookRepository.save(book);
        } else {
            throw new RuntimeException("Book or person not found");
        }
    }

    @Override
    public void freeBook(Long bookId) {
        Optional<Book> bookById = bookRepository.findById(bookId);
        if (bookById.isPresent()) {
            Book book = bookById.get();
            book.setBookOwner(null);
            bookRepository.save(book);
        } else {
            throw new RuntimeException("Book not found");
        }
    }
}
