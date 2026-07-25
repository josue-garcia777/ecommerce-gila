package com.josue.ecommerce.identity.service;

import com.josue.ecommerce.identity.dto.AccessTokenResponse;
import com.josue.ecommerce.identity.dto.LoginRequest;
import com.josue.ecommerce.identity.dto.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthenticationService {
    @Transactional
    AccessTokenResponse register(RegisterRequest request);

    @Transactional(readOnly = true)
    AccessTokenResponse login(LoginRequest request);
}
