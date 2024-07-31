package com.morris.quizly.services;

public interface NotificationService {

    /**
     * Sends a message to Application Admin.
     *
     * @param message {@link String} message to admin
     *
     * @return boolean - message was sent
     */
    boolean notifyAdmin(String message);
}
