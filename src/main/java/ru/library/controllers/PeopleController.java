package ru.library.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.library.dto.PersonDTO;
import ru.library.exceptions.person_exp.PersonErrorResponse;
import ru.library.exceptions.person_exp.PersonNotCreatedException;
import ru.library.exceptions.person_exp.PersonNotFoundException;
import ru.library.models.Person;
import ru.library.services.people_service.PeopleService;
import ru.library.services.people_service.PeopleServiceInf;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/people")
public class PeopleController {

    private final PeopleServiceInf peopleServiceinf;

    private final PeopleService peopleService;


    @Autowired
    public PeopleController(PeopleServiceInf peopleServiceinf, PeopleService peopleService) {
        this.peopleServiceinf = peopleServiceinf;
        this.peopleService = peopleService;
    }

    @GetMapping()
    public List<PersonDTO> getAllPeople() {
        List<Person> allPeople = peopleServiceinf.getAllPeople();

        return allPeople.stream().map(peopleService::convertPersonToPersonDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public PersonDTO getPersonById(@PathVariable("id") Long id) {
        Person personById = peopleServiceinf.findPersonById(id);

        return peopleService.convertPersonToPersonDTO(personById);
    }


    @PostMapping()
    public ResponseEntity<Person> createNewPerson(@RequestBody @Valid PersonDTO personDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for(FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new PersonNotCreatedException(errors.toString());
        }

        Person person = peopleService.convertPersonDTOToPerson(personDTO);

        peopleService.save(person);

        return ResponseEntity.ok(person);

    }

    @PostMapping("/update/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable("id") Long id, @Valid PersonDTO personDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();

            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                errors.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(";");
            }

            throw new PersonNotCreatedException(errors.toString());
        }

        Person person = peopleService.convertPersonDTOToPerson(personDTO);

        peopleService.update(person, id);

        PersonDTO updatedPersonDTO = peopleService.convertPersonToPersonDTO(person);

        return ResponseEntity.ok(updatedPersonDTO);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> softDeletePerson(@PathVariable Long id) {
        peopleService.softDeletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<PersonDTO>> getDeletedPeople() {
        List<Person> deletedPeople = peopleServiceinf.getDeletedPeople();

        return ResponseEntity.ok(deletedPeople.stream().map(peopleService::convertPersonToPersonDTO)
                .toList());
    }

    @ExceptionHandler({PersonNotFoundException.class})
    public ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException ex) {
        PersonErrorResponse response = new PersonErrorResponse(
                ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({PersonNotCreatedException.class})
    public ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException ex) {
        PersonErrorResponse response = new PersonErrorResponse(
                ex.getMessage(), new Date()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
