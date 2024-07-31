package com.morris.quizly.services.impl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.morris.quizly.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("${sns.topicArn}")
    private String snsTopicArn;

    private final AmazonSNS amazonSNSClient;

    public NotificationServiceImpl() {
        this.amazonSNSClient = AmazonSNSClientBuilder.defaultClient();
    }

    @Override
    public boolean notifyAdmin(String message) {
        PublishRequest publishRequest = new PublishRequest(snsTopicArn, message);
        PublishResult publishResult = amazonSNSClient.publish(publishRequest);
        return publishResult.getMessageId() != null;
    }
}
