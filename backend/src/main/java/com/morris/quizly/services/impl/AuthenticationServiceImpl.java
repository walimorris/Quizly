package com.morris.quizly.services.impl;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final AuthenticationManager authenticationManager;
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
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                                     QuizlyUserDetailsService quizlyDetailsService, RecaptchaService recaptchaService,
                                     NotificationService notificationService, PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.quizlyUserDetailsService = quizlyDetailsService;
        this.notificationService = notificationService;
        this.recaptchaService = recaptchaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<?> authenticateLogin(AuthRequest request) {
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
            UserDetails userDetails = (com.morris.quizly.models.security.UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.createToken(userDetails.getUsername(), userDetails.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmailAddress());
            Map<String, Object> response = Map.of(USER_DETAILS, userDetails, ACCESS_TOKEN, token, REFRESH_TOKEN, refreshToken);
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

    @Override
    public ResponseEntity<?> refreshToken(JwtRefreshRequest jwtRefreshRequest) {
        String refreshToken = jwtRefreshRequest.getRefreshToken();
        if (isEmptyRefreshToken(refreshToken)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(MISSING_REFRESH_TOKEN);
        }
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String emailAddress = jwtTokenProvider.getUsername(refreshToken);
                UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(emailAddress);
                if (userDetails != null) {
                    String newAccessToken = jwtTokenProvider.createToken(emailAddress, userDetails.getRoles());
                    String newRefreshToken = jwtTokenProvider.createRefreshToken(emailAddress);

                    JwtRefreshResponse jwtRefreshResponse = JwtRefreshResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken)
                            .userDetails(userDetails)
                            .build();
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(jwtRefreshResponse);
                }
            } else {
                Map<String, Object> errorResponse = Map.of(
                        ERROR, ERROR_REFRESH_TOKEN
                );
                return ResponseEntity.status(403)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
        } catch (UsernameNotFoundException e) {
            LOGGER.error("Error refreshing token, username not found: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                    ERROR, INVALID_USER_NAME
            );
            return ResponseEntity.status(403)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
        return ResponseEntity.badRequest().body(ERROR_UNKNOWN);
    }

    @Override
    public ResponseEntity<?> authenticateSignup(SignupRequest signupRequest) {
        // check username availability
        if (quizlyUserDetailsService.isUserNameInUse(signupRequest.getEmailAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(USERNAME_IN_USE);
        }
        String imageBase64EncodedStr = null;
        if (signupRequest.getImage() != null) {
            imageBase64EncodedStr = FileUtil.base64partEncodedStr(signupRequest.getImage());
        } else {
            LOGGER.warn("Signup request contains a null image");
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

    @Override
    public ResponseEntity<UserDetails> validateOneTimeSessionToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().build();
        }
        String username = jwtTokenProvider.getUsername(token);
        UserDetails user = quizlyUserDetailsService.loadUserByUsername(username);
        if (null != user) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<?> isValidToken(String tokenHeader) {
        String token = tokenHeader.substring(7);
        if (jwtTokenProvider.validateToken(token)) {
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

    @Override
    public ResponseEntity<String> authenticateRecaptcha(RecaptchaRequest recaptchaRequest) {
        String result = recaptchaService.createAssessment(recaptchaRequest.getToken(), "password_reset");
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<String> authenticateResetPassword(PasswordResetRequest resetRequest) {
        try {
            UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(resetRequest.getEmailAddress());
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

    @Override
    public ResponseEntity<String> authenticateResetAndChangePassword(PasswordChangeRequest passwordChangeRequest) {
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

    private boolean isEmptyRefreshToken(String token) {
        return token == null || token.isEmpty();
    }

    private boolean isNonEmptyRefreshToken(String token) {
        return token != null && !token.isEmpty();
    }
}
