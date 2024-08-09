package com.morris.quizly.services;

import com.morris.quizly.models.security.UserDetails;

public interface NotificationService {

    /**
     * Sends a message to Application Admin.
     *
     * @param message {@link String} message to admin
     *
     * @return boolean - message was sent
     */
    boolean notifyAdmin(String message);

    /**
     * Sends signup confirmation email to user.
     *
     * @param user  {@link UserDetails}
     * @param token {@link String} confirmation token
     */
    void sendSignupConfirmationEmailAndLink(UserDetails user, String token);

    /**
     * Sends password reset email to user.
     *
     * @param user  {@link UserDetails}
     * @param token {@link String token} confirmation token
     */
    void sendPasswordResetEmailAndLink(UserDetails user, String token);
}
