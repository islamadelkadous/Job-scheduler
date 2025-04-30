package com.vrtx.scheduler_service.repository;

import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.model.entity.JobEntity;
import com.vrtx.scheduler_service.model.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryFacade {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> findUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't retrieve User having Username: "+ username
            );
        }
    }
}
