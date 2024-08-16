package ru.library.services.admin_service;

import jakarta.transaction.Transactional;
import ru.library.dto.PersonDTO;
import ru.library.models.Book;
import ru.library.models.Person;
import ru.library.services.general_service.GeneralBookServiceInf;
import ru.library.services.general_service.GeneralPeopleServiceInf;

import java.util.List;

public interface AdminServiceInf extends GeneralPeopleServiceInf, GeneralBookServiceInf {

    @Transactional
    void save(Person person);

    @Transactional
    void update(Person person, Long id);

    @Transactional
    void softDeletePerson(Long id);

    @Transactional
    List<Person> getDeletedPeople();

    Person convertPersonDTOToPerson(PersonDTO personDTO);

    void enrichPerson(Person person);

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
    void assignBookToPerson(Long bookId, Long personId);

}
