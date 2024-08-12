package com.morris.quizly.models.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class JwtRefreshRequest {
    private String accessToken;
    private String refreshToken;
    private UserDetails userDetails;
}
