package com.morris.quizly.controllers;

import com.morris.quizly.models.security.*;
import com.morris.quizly.services.AuthenticationService;
import com.morris.quizly.services.NotificationService;
import com.morris.quizly.services.QuizlyUserDetailsService;
import com.morris.quizly.services.RecaptchaService;
import com.morris.quizly.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: I need to refactor much of this logic into a AuthService. This will help decoupling
// TODO: much logic and possible circular dependency
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final QuizlyUserDetailsService quizlyUserDetailsService;
    private final NotificationService notificationService;
    private final RecaptchaService recaptchaService;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_DETAILS = "userDetails";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    private static final String ERROR = "error";
    private static final String ERROR_UNKNOWN = "Unknown error";
    private static final String ERROR_REFRESH_TOKEN = "Invalid refresh token";
    private static final String ERROR_JWT_TOKEN = "Invalid or expired jwt token";
    private static final String ERROR_SIGNUP = "Failed signup";
    private static final String PASSWORD_RESET_ERROR = "Password reset unauthorized";
    private static final String USERNAME_IN_USE = "Email address is already in use.";
    private static final String INVALID_USER_NAME = "Invalid user or email address";
    private static final String MISSING_REFRESH_TOKEN = "Missing refresh token";

    private static final String SIGNUP_SUCCESS = "Signup Successful";
    private static final String SUCCESS = "success";

    @Autowired
    public AuthController(JwtTokenProvider jwtTokenProvider, QuizlyUserDetailsService quizlyDetailsService,
                          RecaptchaService recaptchaService, NotificationService notificationService,
                          PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.quizlyUserDetailsService = quizlyDetailsService;
        this.notificationService = notificationService;
        this.recaptchaService = recaptchaService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute AuthRequest request) {
        try {
            Map<String, Object> authenticationResponse = authenticationService.authenticateLogin(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(authenticationResponse);
        } catch (AuthenticationException e) {
            LOGGER.info("ERROR: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                    ERROR, e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        if (authenticationService.isEmptyRefreshToken(request.get(REFRESH_TOKEN))) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(MISSING_REFRESH_TOKEN);
        }
        Map<String, Object> authenticationResponse = authenticationService.refreshToken(request);
        if (!authenticationResponse.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(authenticationResponse);
        }
        Map<String, Object> errorResponse = Map.of(
                ERROR, ERROR_REFRESH_TOKEN
        );
        return ResponseEntity.status(403)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @GetMapping("/validate_token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        if (authenticationService.isValidToken(tokenHeader)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(true);
        }
        LOGGER.info("Not valid");
        Map<String, Object> errorResponse = Map.of(ERROR, ERROR_JWT_TOKEN);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @GetMapping("/validate-onetime-session-token")
    public ResponseEntity<UserDetails> validateSessionToken(@RequestParam("token") String token) {
        UserDetails user = authenticationService.validateOneTimeSessionToken(token);
        if (null == user) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@ModelAttribute SignupRequest signupRequest) {
        // check username availability
        if (quizlyUserDetailsService.isUserNameInUse(signupRequest.getEmailAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(USERNAME_IN_USE);
        }
        String imageBase64EncodedStr = null;
        try {
            imageBase64EncodedStr = FileUtil.base64partEncodedStr(signupRequest.getImage());
        } catch (NullPointerException e) {
            LOGGER.error("Image is null: {}", e.getMessage());
        }
        LOGGER.info(imageBase64EncodedStr);
        String signupToken = UUID.randomUUID().toString(); // make this secure
        com.morris.quizly.models.security.UserDetails userDetails = com.morris.quizly.models.security.UserDetails.builder()
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .emailAddress(signupRequest.getEmailAddress())
                .username(signupRequest.getEmailAddress())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .roles(List.of(Roles.ROLE_USER))
                .image(imageBase64EncodedStr)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .enabled(false)
                .signupToken(signupToken)
                .build();

        try {
            UserDetails userDetailsResult = quizlyUserDetailsService.save(userDetails);
            if (userDetailsResult.isAccountNonLocked()) {
                notificationService.sendSignupConfirmationEmailAndLink(userDetailsResult, signupToken);
                return ResponseEntity.ok()
                        .body(SIGNUP_SUCCESS);
            }
        } catch (Exception e) {
            LOGGER.error(ERROR_SIGNUP + "{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ERROR_SIGNUP + ": " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ERROR_UNKNOWN);
    }

    @PostMapping("/password-reset/verify-recaptcha")
    public ResponseEntity<String> verifyRecaptcha(@RequestBody RecaptchaRequest request) {
        String result = recaptchaService.createAssessment(request.getToken(), "password_reset");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset/reset-password-request")
    public ResponseEntity<String> resetPasswordRequest(@RequestBody PasswordResetRequest request) {
        try {
            UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(request.getEmailAddress());
            if (null != userDetails) {
                if (userDetails.isAccountNonLocked()) {
                    String token = UUID.randomUUID().toString();
                    quizlyUserDetailsService.updateAndSetPasswordResetToken(userDetails, token);
                    notificationService.sendPasswordResetEmailAndLink(userDetails, token);
                    return ResponseEntity.ok(SUCCESS);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(INVALID_USER_NAME);
            }
        } catch (Exception e) {
            LOGGER.error("Error sending password reset request: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PASSWORD_RESET_ERROR);
    }

    @PostMapping("/password-reset/change-password")
    public ResponseEntity<String> resetAndChangePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        if (!passwordChangeRequest.getPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            LOGGER.error("Passwords do not match!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match.");
        }
        LOGGER.info("Moving into password change process.");
        try {
            String username = jwtTokenProvider.getUsername(passwordChangeRequest.getToken());
            LOGGER.info("User: {}, requesting password change", username);
            UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(username);
            if (null != userDetails) {
                quizlyUserDetailsService.updateAndSetEncodedPassword(
                        userDetails,
                        passwordEncoder.encode(passwordChangeRequest.getPassword())
                );
                return ResponseEntity.ok(SUCCESS);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cannot change password");
            }
        } catch (Exception e) {
            LOGGER.error("Error resetting password: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PASSWORD_RESET_ERROR);
    }
}
