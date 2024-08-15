package ru.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.library.models.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    private String name;
    private Integer age;
    private String email;
    private String phoneNumber;
    private String password;
    private Role role;
}
