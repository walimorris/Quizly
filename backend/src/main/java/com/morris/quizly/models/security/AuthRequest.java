package com.morris.quizly.models.security;

import lombok.*;

/**
 * AuthRequest is used for login process, which expects email and password authentication.
 * Note: email addresses are usernames in the Quizly application.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
    private String emailAddress;
    private String password;
}
