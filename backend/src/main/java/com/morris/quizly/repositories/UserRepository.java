package com.morris.quizly.repositories;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.models.system.SystemFlag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDetails, String> {

    /**
     * Find user by username. Usernames in Quizly are also user email address.
     *
     * @param username {@link String} username
     * @return {@link UserDetails}
     */
    Optional<UserDetails> findByUsername(String username);

    /**
     * Find user by signup token. Signup tokens are used to confirm application subscriptions.
     *
     * @param token {@link String} signup token
     *
     * @return {@link UserDetails}
     */
    Optional<UserDetails> findBySignupToken(String token);

    /**
     * Find user by password reset token. Password tokens expire.
     *
     * @param token {@link String} password reset token
     *
     * @return {@link UserDetails}
     */
    Optional<UserDetails> findByPasswordResetToken(String token);

    /**
     * Determines if an account is non-locked.
     *
     * @param userId {@link String} userId
     *
     * @return boolean
     */
    @Query(value = "{ '_id': ?0 }", fields = "{ 'isAccountNonLocked' : 1 }")
    Boolean isAccountNonLocked(String userId);

    /**
     * Get {@link SystemFlag} list of a user.
     *
     * @param userId {@link String} userId
     *
     * @return {@link List<SystemFlag>}
     */
    @Query(value = "{ '_id': ?0 }", fields = "{ 'flags' : 1 }")
    List<SystemFlag> getSystemFlagsByUserId(String userId);
}
