package com.morris.quizly.services.impl;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.models.system.Flag;
import com.morris.quizly.models.system.SystemFlag;
import com.morris.quizly.repositories.UserRepository;
import com.morris.quizly.services.NotificationService;
import com.morris.quizly.services.SystemFlaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemFlaggingServiceImpl implements SystemFlaggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemFlaggingService.class);

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;

    private static final String ADMIN_LOCK_MESSAGE = "Administration lock on account. Email us at ai.quizly@gmail.com for inquiries.";
    private static final String REASON_NOTIFY_MESSAGE = "User has been notified on (3) occasions for improper system ai usage: \n";

    private static final String ID = "_id";
    private static final String FLAG_COUNT = "flagCount";
    private static final String FLAGS = "flags";
    private static final String ACCOUNT_NON_LOCKED = "accountNonLocked";
    private static final String LOCK_REASON = "lockReason";

    @Autowired
    public SystemFlaggingServiceImpl(UserRepository userRepository, MongoTemplate mongoTemplate,
                                     NotificationService notificationService) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Flag insertFlag(String userId, SystemFlag systemFlag) {
        Query query = new Query(Criteria.where(ID).is(userId));
        Update update = new Update()
                .inc(FLAG_COUNT, 1)
                .push(FLAGS, systemFlag);
        mongoTemplate.updateFirst(query, update, UserDetails.class);

        UserDetails userDetails = mongoTemplate.findOne(query, UserDetails.class);
        if (null != userDetails && userDetails.getFlagCount() >= 3) {
            String lockReason = createLockReason(userDetails.getFlags());
            return lockAccount(userId, lockReason);
        }
        return null != userDetails ? getFlagResult(userDetails.getFlagCount()) : null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Flag lockAccount(String userId, String lockReason) {
        Query query = new Query(Criteria.where(ID).is(userId));
        Update update = new Update()
                .set(ACCOUNT_NON_LOCKED, false)
                .set(LOCK_REASON, lockReason);

        mongoTemplate.updateFirst(query, update, UserDetails.class);
        boolean isAdminNotified = notifyAdminOfAccountLock(userId, lockReason);
        if (isAdminNotified) {
            LOGGER.info("Admin notified of account lock for user '{}' with reason: {}", userId, lockReason);
        }
        return Flag.FLAG_LOCK;
    }

    @Override
    public String createLockReason(List<SystemFlag> systemFlags) {
        if (systemFlags.isEmpty()) {
            return ADMIN_LOCK_MESSAGE;
        }
        int mark = 1;
        StringBuilder reason = new StringBuilder();
        reason.append(REASON_NOTIFY_MESSAGE);
        for (SystemFlag flag : systemFlags) {
            reason.append(mark).append(". ").append(flag.getFlagType()).append("\n");
            mark++;
        }
        return reason.toString();
    }

    @Override
    public boolean notifyAdminOfAccountLock(String userId, String lockReason) {
        String message = String.format(
                "User '%s' has been account locked for reason: %s",
                userId, lockReason
        );
        return notificationService.notifyAdmin(message);
    }

    @Override
    public boolean isAccountNonLocked(String userId) {
        return userRepository.isAccountNonLocked(userId);
    }

    @Override
    public List<SystemFlag> getSystemFlagsByUser(String userId) {
        return userRepository.getSystemFlagsByUserId(userId);
    }

    private Flag getFlagResult(int flagCount) {
        return switch (flagCount) {
            case 1 -> Flag.FLAG_ONE;
            case 2 -> Flag.FLAG_TWO;
            default -> Flag.FLAG_LOCK;
        };
    }

    private void doSomething() {
        LOGGER.info("Add user notification and locked logout procedure.");
    }
}
