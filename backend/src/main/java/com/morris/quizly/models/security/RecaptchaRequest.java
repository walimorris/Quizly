package com.morris.quizly.models.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecaptchaRequest {
    private String token;
}
