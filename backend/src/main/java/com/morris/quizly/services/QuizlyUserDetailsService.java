package com.morris.quizly.services;

import com.morris.quizly.models.security.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Interface provides functionality for Quizly users such as user retrieval, creations,
 * deletion, and update.
 */
public interface QuizlyUserDetailsService extends UserDetailsService {

    /**
     * Get user by username.
     *
     * @param username {@link String} username. Usernames are also user email addresses.
     *
     * @return {@link UserDetails}
     */
    UserDetails loadUserByUsername(String username);

    /**
     * Save and persist user.
     *
     * @param userDetails {@link UserDetails}
     *
     * @return {@link UserDetails}
     */
    UserDetails save(UserDetails userDetails);

    /**
     * Check if username is available.
     *
     * @param username {@link String} username
     *
     * @return boolean
     */
    boolean isUserNameInUse(String username);

    /**
     * Find user by signup token.
     *
     * @param token {@link String} signup token
     *
     * @return {@link UserDetails}
     */
    UserDetails findBySignupToken(String token);

    /**
     * Find by password reset token.
     *
     * @param token {@link String} reset token
     *
     * @return {@link UserDetails}
     */
    UserDetails findByPasswordResetToken(String token);

    /**
     * Modifies user enabled status.
     *
     * @param user {@link UserDetails} user
     * @param enabled boolean
     */
    void updateEnabledStatus(UserDetails user, boolean enabled);

    /**
     * Modifies user password reset token.
     *
     * @param user {@link UserDetails} user
     * @param token {@link String} token
     */
    void updateAndSetPasswordResetToken(UserDetails user, String token);

    /**
     * Modifies and removes user password reset token and expiry TTL.
     *
     * @param token {@link String} password reset token
     */
    void updateAndRemovePasswordResetToken(String token);
}
