package com.morris.quizly.services;

import com.morris.quizly.models.system.Flag;
import com.morris.quizly.models.system.SystemFlag;

import java.util.List;

public interface SystemFlaggingService {

    /**
     * Insert Flag on user account.
     *
     * @param userId     {@link String} userId
     * @param systemFlag {@link SystemFlag}
     *
     * @return {@link Flag}
     */
    Flag insertFlag(String userId, SystemFlag systemFlag);

    /**
     * Get flags {@link List<SystemFlag>} for user.
     *
     * @param userId {@link String} userId
     *
     * @return {@link List<SystemFlag>}
     */
    List<SystemFlag> getSystemFlagsByUser(String userId);

    /**
     * Lock user account.
     *
     * @param userId     {@link String} userId
     * @param lockReason {@link String} lock reason
     *
     * @return {@link Flag}
     */
    Flag lockAccount(String userId, String lockReason);

    /**
     * Get is account non-locked state for user.
     *
     * @param userId {@link String} userId
     *
     * @return boolean
     */
    boolean isAccountNonLocked(String userId);

    /**
     * Create account lock reason.
     *
     * @param systemFlags flags {@link List<SystemFlag>}
     *
     * @return {@link String} account lock reason
     */
    String createLockReason(List<SystemFlag> systemFlags);

    /**
     * Notify Admin of account lock.
     *
     * @param userId     {@link String} userId
     * @param lockReason {@link String} account lock reason
     *
     * @return boolean
     */
    boolean notifyAdminOfAccountLock(String userId, String lockReason);
}
