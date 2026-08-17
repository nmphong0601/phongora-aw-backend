package com.aw.auth.entity;

import lombok.*;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean isActive;
    private UUID employeeId;
    private Set<String> roles; // Sẽ được xử lý map qua MyBatis ResultMap
}