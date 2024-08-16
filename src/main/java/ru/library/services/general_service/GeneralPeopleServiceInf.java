package ru.library.services.general_service;

import jakarta.transaction.Transactional;
import ru.library.dto.BookDTO;
import ru.library.dto.PersonDTO;
import ru.library.models.Person;

import java.util.List;

public interface GeneralPeopleServiceInf {
    @Transactional
    List<Person> getAllPeople();

    @Transactional
    Person findPersonById(Long id);

    @Transactional
    List<BookDTO> getBooksByPersonId(Long id);

    PersonDTO convertPersonToPersonDTO(Person person);

//    Person convertPersonDTOToPerson(PersonDTO personDTO);
//
//    void enrichPerson(Person person);
}
