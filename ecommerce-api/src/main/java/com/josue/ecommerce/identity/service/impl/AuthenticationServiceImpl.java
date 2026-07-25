package com.josue.ecommerce.identity.service.impl;

import com.josue.ecommerce.identity.domain.Role;
import com.josue.ecommerce.identity.domain.User;
import com.josue.ecommerce.identity.domain.UserRole;
import com.josue.ecommerce.identity.dto.AccessTokenResponse;
import com.josue.ecommerce.identity.dto.LoginRequest;
import com.josue.ecommerce.identity.dto.RegisterRequest;
import com.josue.ecommerce.identity.mapper.IdentityMapper;
import com.josue.ecommerce.identity.repository.UserAccountRepository;
import com.josue.ecommerce.identity.repository.UserRoleRepository;
import com.josue.ecommerce.identity.repository.specification.UserRoleSpecifications;
import com.josue.ecommerce.identity.repository.specification.UserSpecifications;
import com.josue.ecommerce.identity.service.AuthenticationService;
import com.josue.ecommerce.identity.service.JwtTokenService;
import com.josue.ecommerce.shared.ValueObjects.Address;
import com.josue.ecommerce.shared.error.ApiException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final IdentityMapper identityMapper;

    public AuthenticationServiceImpl(UserAccountRepository userAccountRepository, UserRoleRepository userRoleRepository,
                                     PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService,
                                     IdentityMapper identityMapper) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.identityMapper = identityMapper;
    }

    @Transactional
    @Override
    public AccessTokenResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());

        if (userAccountRepository.findOne(UserSpecifications.hasEmail(email)).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered",
                    "An account already exists for this email address");
        }

        User user = toUser(request, email);

        User savedUser = userAccountRepository.save(user);

        Set<Role> roles = Set.of(Role.CUSTOMER);

        userRoleRepository.save(new UserRole(savedUser, Role.CUSTOMER));

        return accessToken(savedUser, roles);
    }

    @Transactional(readOnly = true)
    @Override
    public AccessTokenResponse login(LoginRequest request) {
        String email = User.normalizeEmail(request.email());

        User user = userAccountRepository.findOne(UserSpecifications.hasEmail(email))
                .filter(User::isEnabled)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(this::invalidCredentials);

        Set<Role> roles = new HashSet<>();

        userRoleRepository.findAll(UserRoleSpecifications.hasUserId(user.getId()))
                .forEach(userRole -> roles.add(userRole.getRole()));

        return accessToken(user, roles);
    }

    private User toUser(RegisterRequest request, String email) {
        return new User(email,
                passwordEncoder.encode(request.password()),
                Instant.now(),
                new Address(request.address().line1(), request.address().line2(), request.address().city(), request.address().state(), request.address().postalCode(),
                        request.address().countryCode()
                ));
    }

    private AccessTokenResponse accessToken(User user, Set<Role> roles) {
        JwtTokenService.IssuedAccessToken token = jwtTokenService.issue(user.getId(), roles);
        return new AccessTokenResponse(token.value(), "Bearer", token.expiresAt(), identityMapper.toResponse(user, roles));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials",
                "The email or password is incorrect");
    }
}
