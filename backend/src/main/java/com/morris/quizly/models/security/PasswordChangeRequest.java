package com.morris.quizly.models.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    private String password;
    private String confirmPassword;
    private String token;
}
