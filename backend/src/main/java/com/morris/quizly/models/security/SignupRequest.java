package com.morris.quizly.models.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@Builder
public class SignupRequest {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private MultipartFile image;
}
