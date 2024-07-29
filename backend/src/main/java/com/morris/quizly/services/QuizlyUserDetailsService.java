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
     * @return {@link UserDetails}
     */
    UserDetails save(UserDetails userDetails);

    /**
     * Check if username is available.
     *
     * @param username {@link String} username
     * @return boolean
     */
    boolean isUserNameInUse(String username);
}
