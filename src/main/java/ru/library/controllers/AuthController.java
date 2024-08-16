package ru.library.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.library.dto.AuthDTO;
import ru.library.dto.PersonDTO;
import ru.library.models.Person;
import ru.library.models.Role;
import ru.library.security.PersonDetails;
import ru.library.services.admin_service.AdminService;
import ru.library.services.people_service.PeopleService;
import ru.library.util.JWTUtil;
import ru.library.validation.PersonValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AdminService adminService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final PersonValidator personValidator;
    private final AuthenticationManager authenticationManager;


    @Autowired
    public AuthController(PeopleService peopleService, AdminService adminService, JWTUtil jwtUtil, ModelMapper modelMapper, PersonValidator personValidator, AuthenticationManager authenticationManager) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.personValidator = personValidator;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody AuthDTO authDTO) {
        UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(authDTO.getName(), authDTO.getPassword());

        try {
            Authentication authenticate = authenticationManager.authenticate(userToken);

            PersonDetails personDetails = (PersonDetails) authenticate.getPrincipal();

            String token = jwtUtil.generateToken(personDetails.getUsername(), personDetails.getPerson().getRole());

            return Map.of("jwt-token", token);
        } catch (AuthenticationException e) {
            return Map.of("error", "incorrect login or password");
        }
    }

    @PostMapping("/registration")
    public Map<String, String> registration(@RequestBody @Valid PersonDTO personDTO,
                                            BindingResult bindingResult) {

        if (personDTO.getRole() == null) {
            personDTO.setRole(Role.ROLE_USER);
        }

        Person person = adminService.convertPersonDTOToPerson(personDTO);

        personValidator.validate(person, bindingResult);

        if(bindingResult.hasErrors()){
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return Map.of("message", errors.toString());
        }


        adminService.save(person);

        String token = jwtUtil.generateToken(person.getName(), person.getRole());

        return Map.of("jwt-token", token);
    }

    @GetMapping("/show")
    public String showAuthenticatedUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails principal = (PersonDetails) authentication.getPrincipal();
        return principal.getUsername();
    }

}

