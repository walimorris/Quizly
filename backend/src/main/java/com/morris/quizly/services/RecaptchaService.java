package com.morris.quizly.services;

public interface RecaptchaService {

    /**
     * Creates an assessment to analyze the risk of a UI action for reCAPTCHA.
     *
     * @param token The generated token obtained from the client.
     * @param action Action name corresponding to the token.
     *
     * @return A string describing the result of the assessment, including the reCAPTCHA score and reasons.
     */
    String createAssessment(String token, String action);
}
