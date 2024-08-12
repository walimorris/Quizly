package com.morris.quizly.services;

import com.morris.quizly.models.security.*;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    /**
     * Authenticate login request.
     *
     * @param authRequest {@link AuthRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<?> authenticateLogin(AuthRequest authRequest);

    /**
     * Authenticate signup request.
     *
     * @param signupRequest {@link SignupRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<?> authenticateSignup(SignupRequest signupRequest);

    /**
     * Process JWT refresh request.
     *
     * @param jwtRefreshRequest {@link JwtRefreshRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<?> refreshToken(JwtRefreshRequest jwtRefreshRequest);

    /**
     * Examines a token and identifies if is a valid token.
     *
     * @param tokenHeader {@link String} token
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<?> isValidToken(String tokenHeader);

    /**
     * Validates a one time session token. These are known short-lived tokens.
     *
     * @param token {@link String} short-lived token
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<UserDetails> validateOneTimeSessionToken(String token);

    /**
     * Authenticate reCAPTCHA request.
     *
     * @param recaptchaRequest {@link RecaptchaRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<String> authenticateRecaptcha(RecaptchaRequest recaptchaRequest);

    /**
     * Authenticate Password Reset request. This does not reset password, it only authenticates
     * if a reset request is valid or not.
     *
     * @param passwordResetRequest {@link PasswordResetRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<String> authenticateResetPassword(PasswordResetRequest passwordResetRequest);

    /**
     * Authenticates PasswordReset and Change request. This method updates and persists password change.
     *
     * @param passwordChangeRequest {@link PasswordChangeRequest}
     *
     * @return {@link ResponseEntity}
     */
    ResponseEntity<String> authenticateResetAndChangePassword(PasswordChangeRequest passwordChangeRequest);
}
