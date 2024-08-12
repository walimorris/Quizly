package com.morris.quizly.controllers;

import com.morris.quizly.models.security.*;
import com.morris.quizly.services.AuthenticationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// TODO: more robust error logging on bound requests models
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @ModelAttribute AuthRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.authenticateLogin(request);
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(@Valid @ModelAttribute JwtRefreshRequest jwtRefreshRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.refreshToken(jwtRefreshRequest);
    }

    @GetMapping("/validate_token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        return authenticationService.isValidToken(tokenHeader);
    }

    @GetMapping("/validate-onetime-session-token")
    public ResponseEntity<UserDetails> validateSessionToken(@RequestParam("token") String token) {
        return authenticationService.validateOneTimeSessionToken(token);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest signupRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.authenticateSignup(signupRequest);
    }

    @PostMapping("/password-reset/verify-recaptcha")
    public ResponseEntity<?> verifyRecaptcha(@Valid @RequestBody RecaptchaRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.authenticateRecaptcha(request);
    }

    @PostMapping("/password-reset/reset-password-request")
    public ResponseEntity<?> resetPasswordRequest(@Valid @RequestBody PasswordResetRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.authenticateResetPassword(request);
    }

    @PostMapping("/password-reset/change-password")
    public ResponseEntity<?> resetAndChangePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authenticationService.authenticateResetAndChangePassword(passwordChangeRequest);
    }
}
