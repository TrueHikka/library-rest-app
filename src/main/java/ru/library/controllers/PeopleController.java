package ru.library.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.library.dto.BookDTO;
import ru.library.dto.PersonDTO;
import ru.library.models.Person;
import ru.library.services.people_service.PeopleService;

import java.util.List;

@RestController
@RequestMapping("api/people")
public class PeopleController {
    private final PeopleService peopleService;

    @Autowired
    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping()
    public List<PersonDTO> getAllPeople() {
        List<Person> allPeople = peopleService.getAllPeople();

        return allPeople.stream()
                .map(peopleService::convertPersonToPersonDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public PersonDTO getPersonById(@PathVariable("id") Long id) {
        Person personById = peopleService.findPersonById(id);

        return peopleService.convertPersonToPersonDTO(personById);
    }

    @GetMapping("/personsBook/{id}")
    public ResponseEntity<List<BookDTO>> getBooksByPersonId(@PathVariable("id") Long id) {
        List<BookDTO> booksByPersonId = peopleService.getBooksByPersonId(id);
        return ResponseEntity.ok(booksByPersonId);
    }
}
