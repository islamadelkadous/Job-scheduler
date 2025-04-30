package com.vrtx.scheduler_service.service;

import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.repository.UserRepositoryFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepositoryFacade userRepositoryFacade;

    @Autowired
    public JwtUserDetailsService(UserRepositoryFacade userRepositoryFacade) {
        this.userRepositoryFacade = userRepositoryFacade;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepositoryFacade.findUserByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "UNAUTHORIZED", "User not found with username: " + username));
    }
}
