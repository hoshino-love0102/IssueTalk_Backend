package com.issuetalk.user.repository;

import com.issuetalk.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
