package com.morris.quizly.services.impl;

import com.morris.quizly.models.security.AuthRequest;
import com.morris.quizly.models.security.JwtTokenProvider;
import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.services.AuthenticationService;
import com.morris.quizly.services.NotificationService;
import com.morris.quizly.services.QuizlyUserDetailsService;
import com.morris.quizly.services.RecaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> authenticateLogin(AuthRequest request) {
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
        return Map.of(USER_DETAILS, userDetails, ACCESS_TOKEN, token, REFRESH_TOKEN, refreshToken);
    }

    @Override
    public Map<String, Object> refreshToken(Map<String, String> request) {
        String refreshToken = request.get(REFRESH_TOKEN);
        if (isNonEmptyRefreshToken(refreshToken)) {
            try {
                if (jwtTokenProvider.validateToken(refreshToken)) {
                    String emailAddress = jwtTokenProvider.getUsername(refreshToken);
                    UserDetails userDetails = quizlyUserDetailsService.loadUserByUsername(emailAddress);
                    if (userDetails != null) {
                        String newAccessToken = jwtTokenProvider.createToken(emailAddress, userDetails.getRoles());
                        String newRefreshToken = jwtTokenProvider.createRefreshToken(emailAddress);
                        return Map.of(ACCESS_TOKEN, newAccessToken, REFRESH_TOKEN, newRefreshToken, USER_DETAILS, userDetails);
                    }
                }
            } catch (UsernameNotFoundException e) {
                LOGGER.error("Error refreshing token, username not found: {}", e.getMessage());
            }
        }
        return new HashMap<>();
    }

    public UserDetails validateOneTimeSessionToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return null;
        }
        String username = jwtTokenProvider.getUsername(token);
        return quizlyUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public boolean isValidToken(String tokenHeader) {
        String token = tokenHeader.substring(7);
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public boolean isEmptyRefreshToken(String token) {
        return token == null || token.isEmpty();
    }

    @Override
    public boolean isNonEmptyRefreshToken(String token) {
        return token != null && !token.isEmpty();
    }
}
