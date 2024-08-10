package com.morris.quizly.services;

import com.morris.quizly.models.security.AuthRequest;
import com.morris.quizly.models.security.UserDetails;

import java.util.Map;

public interface AuthenticationService {

    /**
     * Authenticate user login.
     *
     * @param request {@link AuthRequest}
     *
     * @return {@link Map} response
     */
    Map<String, Object> authenticateLogin(AuthRequest request);

    /**
     * Process and refresh jwt token.
     *
     * @param request {@link Map} request
     *
     * @return {@link Map} response
     */
    Map<String, Object> refreshToken(Map<String, String> request);

    /**
     * Validates token from given clientside tokenHeader.
     *
     * @param tokenHeader {@link String} tokenHeader
     *
     * @return boolean
     */
    boolean isValidToken(String tokenHeader);

    /**
     * Validates a short-lived onetime session token.
     *
     * @param token {@link String} token
     *
     * @return {@link UserDetails} the user token is assigned to
     */
    UserDetails validateOneTimeSessionToken(String token);

    /**
     * Check if token is empty.
     *
     * @param token {@link String}
     *
     * @return boolean
     */
    boolean isEmptyRefreshToken(String token);

    /**
     * Check if token is not empty.
     *
     * @param token {@link String}
     *
     * @return boolean
     */
    boolean isNonEmptyRefreshToken(String token);
}
