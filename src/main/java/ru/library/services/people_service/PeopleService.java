package ru.library.services.people_service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.library.dto.BookDTO;
import ru.library.dto.PersonDTO;
import ru.library.exceptions.person_exp.PersonNotFoundException;
import ru.library.models.Person;
import ru.library.models.Role;
import ru.library.repositories.PeopleRepository;
import ru.library.services.book_service.BookService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PeopleService implements PeopleServiceInf{
    private final PeopleRepository peopleRepository;
    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository, BookService bookService, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.peopleRepository = peopleRepository;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
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
    public void save(Person person) {
        if (person.getRole() == null) {
            person.setRole(Role.ROLE_USER);
        }

        peopleRepository.save(person);
    }

    @Override
    public void update(Person person, Long id) {
        person.setId(id);
        peopleRepository.save(person);
    }

    @Override
    public void softDeletePerson(Long id) {
        Person person = peopleRepository.findById(id).orElseThrow();
        person.setRemovedAt(LocalDateTime.now());
        peopleRepository.save(person);
    }

    @Override
    public List<Person> getDeletedPeople() {
        return peopleRepository.findByRemovedAtNotNull();
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

    public Person convertPersonDTOToPerson(PersonDTO personDTO) {
        Person person = modelMapper.map(personDTO, Person.class);

        enrichPerson(person);

        return person;
    }

    private void enrichPerson(Person person) {
        person.setRole(person.getRole());
        person.setCreatedAt(LocalDateTime.now());
        person.setRemovedAt(null);
        person.setCreatedPerson("ADMIN");
        person.setRemovedPerson(null);
        person.setPassword(passwordEncoder.encode(person.getPassword()));
    }

}
