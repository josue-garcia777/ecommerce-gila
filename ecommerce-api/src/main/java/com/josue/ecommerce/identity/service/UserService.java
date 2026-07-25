package com.josue.ecommerce.identity.service;

import com.josue.ecommerce.identity.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserService {
    @Transactional(readOnly = true)
    User getUser(UUID userId);
}
