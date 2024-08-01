package com.morris.quizly.services;

public interface EmailService {

    /**
     * Sends email.
     *
     * @param to      {@link String} email recipient
     * @param subject {@link String} Subject
     * @param body    {@link String} email content
     */
    void sendEmail(String to, String subject, String body);
}
