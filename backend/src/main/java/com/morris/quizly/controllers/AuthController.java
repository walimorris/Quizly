package com.morris.quizly.controllers;

import com.morris.quizly.models.security.*;
import com.morris.quizly.services.NotificationService;
import com.morris.quizly.services.QuizlyUserDetailsService;
import com.morris.quizly.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final QuizlyUserDetailsService quizlyUserDetailsService;
    private final NotificationService notificationService;

    private static final String USER_DETAILS = "userDetails";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    private static final String ERROR = "error";
    private static final String ERROR_UNKNOWN = "Unknown error";
    private static final String ERROR_REFRESH_TOKEN = "Invalid refresh token";
    private static final String ERROR_JWT_TOKEN = "Invalid or expired jwt token";
    private static final String ERROR_SIGNUP = "Failed signup";
    private static final String USERNAME_IN_USE = "Email address is already in use.";
    private static final String MISSING_REFRESH_TOKEN = "Missing refresh token";

    private static final String SIGNUP_SUCCESS = "Signup Successful";


    @Autowired
    public AuthController(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider, QuizlyUserDetailsService quizlyDetailsService,
                          NotificationService notificationService) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.quizlyUserDetailsService = quizlyDetailsService;
        this.notificationService = notificationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute AuthRequest request) {
        try {
            // Note: Springboot reads the properties on the user object: accountNonExpired, accountNonLocked, CredentialsNonExpired
            // and will not authenticate users if these properties are not in the correct state and instead will return values such
            // as 'Bad credentials', 'User Account is locked' etc
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmailAddress(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Note: getUserName() and getEmailAddress() are being interchanged. This is because quizly usernames
            // are indeed their emailAddress. You can see this below in the signup process.
            com.morris.quizly.models.security.UserDetails userDetails = (com.morris.quizly.models.security.UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.createToken(userDetails.getUsername(), userDetails.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmailAddress());

            Map<String, Object> response = new HashMap<>();
            response.put(USER_DETAILS, userDetails);
            response.put(ACCESS_TOKEN, token);
            response.put(REFRESH_TOKEN, refreshToken);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
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
        String refreshToken = request.get(REFRESH_TOKEN);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(MISSING_REFRESH_TOKEN);
        }
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String emailAddress = jwtTokenProvider.getUsername(refreshToken);
                com.morris.quizly.models.security.UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(emailAddress);
                if (userDetails == null) {
                    return ResponseEntity
                            .status(403)
                            .body(ERROR_REFRESH_TOKEN);
                }
                String newAccessToken = jwtTokenProvider.createToken(emailAddress, userDetails.getRoles());
                String newRefreshToken = jwtTokenProvider.createRefreshToken(emailAddress);
                return ResponseEntity.ok(Map.of(
                        ACCESS_TOKEN, newAccessToken,
                        REFRESH_TOKEN, newRefreshToken,
                        USER_DETAILS, userDetails
                ));
            } else {
                Map<String, Object> errorResponse = Map.of(
                        ERROR, ERROR_REFRESH_TOKEN
                );
                return ResponseEntity.status(403)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
        } catch (UsernameNotFoundException e) {
            Map<String, Object> errorResponse = Map.of(
                    ERROR, ERROR_REFRESH_TOKEN
            );
            return ResponseEntity.status(403)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @GetMapping("/validate_token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.substring(7);
        boolean isValid = jwtTokenProvider.validateToken(token);
        if (isValid) {
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
}
