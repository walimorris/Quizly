package com.morris.quizly.services.impl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.services.EmailService;
import com.morris.quizly.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("${sns.systemsFlaggingTopicArn}")
    private String snsSystemsFlaggingTopicArn;

    private final AmazonSNS amazonSNSClient;
    private final EmailService emailService;

    private static final String DEV_CONFIRMATION_LINK = "http://localhost:8081/api/subscriptions/confirm-signup?token=";
    private static final String DEV_PASSWORD_RESET_LINK = "http://localhost:8081/api/subscriptions/password-reset?token=";

    public NotificationServiceImpl(EmailService emailService) {
        this.amazonSNSClient = AmazonSNSClientBuilder.defaultClient();
        this.emailService = emailService;
    }

    @Override
    public boolean notifyAdmin(String message) {
        PublishRequest publishRequest = new PublishRequest(snsSystemsFlaggingTopicArn, message);
        PublishResult publishResult = amazonSNSClient.publish(publishRequest);
        return publishResult.getMessageId() != null;
    }

    @Override
    public void sendSignupConfirmationEmailAndLink(UserDetails user, String token) {
        try {
            sendCustomConfirmationEmail(user, token);
        } catch (Exception e) {
            LOGGER.error("Error sending signup confirmation email: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmailAndLink(UserDetails user, String token) {
        try {
            sendCustomPasswordResetEmail(user, token);
        } catch (Exception e) {
            LOGGER.error("Error sending signup confirmation email: {}", e.getMessage());
        }
    }

    private void sendCustomPasswordResetEmail(UserDetails user, String token) {
        String passwordResetLink = DEV_PASSWORD_RESET_LINK + token;
        String emailBody = String.format(
                "Hi, " + user.getFirstName() + ", we've received a notification for password reset on your Quizly account.\n" +
                        "Please use the below link to continue this process. If you've not requested this reset process, email " +
                        "our team at ai.quizly@gmail.com to report.\n\n" + passwordResetLink
        );
        emailService.sendEmail(user.getEmailAddress(), "Quizly Password Reset Request", emailBody);
    }

    private void sendCustomConfirmationEmail(UserDetails user, String token) {
        String confirmationLink = DEV_CONFIRMATION_LINK + token;
        String emailBody = String.format(
                "Welcome to Quizly, %s! We're excited you've decided to join this educational journey with us.\n" +
                        "Please confirm your subscription by clicking the link below:\n\n" + confirmationLink, user.getFirstName()
        );
        emailService.sendEmail(user.getEmailAddress(), "Quizly Signup Confirmation", emailBody);
    }
}
