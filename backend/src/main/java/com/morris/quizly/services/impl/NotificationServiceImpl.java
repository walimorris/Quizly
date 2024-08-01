package com.morris.quizly.services.impl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
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

    @Value("${sns.applicationUserTopicArn}")
    private String snsApplicationUserTopicArn;

    private final AmazonSNS amazonSNSClient;
    private final EmailService emailService;

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
    public boolean subscribeUserToAppUserTopic(String userEmailAddress) {
        try {
            SubscribeRequest subscribeRequest = new SubscribeRequest()
                    .withTopicArn(snsApplicationUserTopicArn)
                    .withProtocol("email")
                    .withEndpoint(userEmailAddress);
            SubscribeResult subscribeResult = amazonSNSClient.subscribe(subscribeRequest);
            sendCustomConfirmationEmail(userEmailAddress, subscribeResult.getSubscriptionArn());
            return true;
        } catch (Exception e) {
            LOGGER.error("Error subscribing user to Application User SNS Topic: {}", e.getMessage());
            return false;
        }
    }

    private void sendCustomConfirmationEmail(String userEmail, String subscriptionArn) {}
}
