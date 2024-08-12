package com.morris.quizly.services;

import com.morris.quizly.models.security.*;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<?> authenticateLogin(AuthRequest request);
    ResponseEntity<?> authenticateSignup(SignupRequest signupRequest);
    ResponseEntity<?> refreshToken(JwtRefreshRequest jwtRefreshRequest);
    ResponseEntity<?> isValidToken(String tokenHeader);
    ResponseEntity<UserDetails> validateOneTimeSessionToken(String token);
    ResponseEntity<String> authenticateRecaptcha(RecaptchaRequest recaptchaRequest);
    ResponseEntity<String> authenticateResetPassword(PasswordResetRequest resetRequest);
    ResponseEntity<String> authenticateResetAndChangePassword(PasswordChangeRequest passwordChangeRequest);
}
