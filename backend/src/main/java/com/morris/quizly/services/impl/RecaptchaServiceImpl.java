package com.morris.quizly.services.impl;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.*;
import com.morris.quizly.models.security.ConfigurationComponent;
import com.morris.quizly.services.RecaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecaptchaServiceImpl implements RecaptchaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaServiceImpl.class);

    private final ConfigurationComponent configurationComponent;
    private final RecaptchaEnterpriseServiceClient recaptchaEnterpriseServiceClient;

    @Autowired
    public RecaptchaServiceImpl(ConfigurationComponent configurationComponent, RecaptchaEnterpriseServiceClient recaptchaEnterpriseServiceClient) {
        this.configurationComponent = configurationComponent;
        this.recaptchaEnterpriseServiceClient = recaptchaEnterpriseServiceClient;
    }

    @Override
    public String createAssessment(String token, String action) {
        Event event = Event.newBuilder()
                .setSiteKey(configurationComponent.getRecaptchaSiteKey())
                .setToken(token)
                .build();

        CreateAssessmentRequest createAssessmentRequest = CreateAssessmentRequest.newBuilder()
                .setParent(ProjectName.of(configurationComponent.getRecaptchaProjectId()).toString())
                .setAssessment(Assessment.newBuilder().setEvent(event).build())
                .build();

        Assessment response = recaptchaEnterpriseServiceClient.createAssessment(createAssessmentRequest);
        if (!response.getTokenProperties().getValid()) {
            return "The CreateAssessment call failed because the token was: " +
                    response.getTokenProperties().getInvalidReason().name();
        }

        if (!response.getTokenProperties().getAction().equals(action)) {
            return "The action attribute in reCAPTCHA tag is: "
                    + response.getTokenProperties().getAction()
                    + "The action attribute in the reCAPTCHA tag "
                    + "does not match the action ("
                    + action
                    + ") you are expecting to score";
        }
        float recaptchaScore = response.getRiskAnalysis().getScore();
        StringBuilder reasons = new StringBuilder("The reCAPTCHA score is: " + recaptchaScore + "\nReasons: ");
        for (RiskAnalysis.ClassificationReason reason : response.getRiskAnalysis().getReasonsList()) {
            reasons.append(reason.name()).append(" ");
        }
        String assessmentName = response.getName();
        reasons.append("\nAssessment name: ").append(assessmentName.substring(assessmentName.lastIndexOf("/") + 1));
        return reasons.toString();
    }
}
