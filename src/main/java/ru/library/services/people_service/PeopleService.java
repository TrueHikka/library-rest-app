package ru.library.services.people_service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.library.dto.BookDTO;
import ru.library.dto.PersonDTO;
import ru.library.exceptions.person_exp.PersonNotFoundException;
import ru.library.models.Person;
import ru.library.repositories.PeopleRepository;
import ru.library.services.book_service.BookService;
import ru.library.services.general_service.GeneralPeopleServiceInf;

import java.util.List;
import java.util.Optional;

@Service
public class PeopleService implements GeneralPeopleServiceInf {
    private final PeopleRepository peopleRepository;
    private final BookService bookService;
    private final ModelMapper modelMapper;
    @Autowired
    public PeopleService(PeopleRepository peopleRepository, BookService bookService, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.peopleRepository = peopleRepository;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Person> getAllPeople() {
        return  peopleRepository.findAll();
    }

    @Override
    public Person findPersonById(Long id) {
        Optional<Person> personById = peopleRepository.findById(id);

        return personById.orElseThrow(() -> new PersonNotFoundException("Person with this id is not found"));
    }

    @Override
    public List<BookDTO> getBooksByPersonId(Long id) {
        Optional<Person> personById = peopleRepository.findById(id);
        if (personById.isPresent()) {
            return personById.get().getBooks().stream()
                    .map(bookService::convertBookToBookDTO)
                    .toList();
        } else {
            throw new RuntimeException("Person not found");
        }
    }

    public PersonDTO convertPersonToPersonDTO(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }
}
