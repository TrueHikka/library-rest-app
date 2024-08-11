package ru.library.services.people_service;

import jakarta.transaction.Transactional;
import ru.library.dto.PersonDTO;
import ru.library.models.Person;

import java.util.List;

public interface PeopleServiceInf {
    @Transactional
    List<Person> getAllPeople();

    @Transactional
    Person findPersonById(Long id);

    @Transactional
    void save(Person person);

    @Transactional
    void update(Person person, Long id);

    @Transactional
    void softDeletePerson(Long id);

    @Transactional
    List<Person> getDeletedPeople();
}
