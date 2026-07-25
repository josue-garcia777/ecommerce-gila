package com.josue.ecommerce.identity.service.impl;

import com.josue.ecommerce.identity.domain.User;
import com.josue.ecommerce.identity.repository.UserAccountRepository;
import com.josue.ecommerce.identity.service.UserService;
import com.josue.ecommerce.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserAccountServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public User getUser(UUID userId) {
        return userAccountRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required",
                        "The authenticated user is unavailable"));
    }
}
