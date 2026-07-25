package com.josue.ecommerce.identity.service.impl;

import com.josue.ecommerce.identity.config.InitAdminProperties;
import com.josue.ecommerce.identity.domain.Role;
import com.josue.ecommerce.identity.domain.User;
import com.josue.ecommerce.identity.domain.UserRole;
import com.josue.ecommerce.identity.repository.UserAccountRepository;
import com.josue.ecommerce.identity.repository.UserRoleRepository;
import com.josue.ecommerce.identity.repository.specification.UserSpecifications;
import com.josue.ecommerce.identity.service.InitAdminService;
import com.josue.ecommerce.shared.ValueObjects.Address;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InitAdminServiceImpl implements InitAdminService {

    private final InitAdminProperties properties;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public InitAdminServiceImpl(InitAdminProperties properties, UserAccountRepository userAccountRepository,
                                UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void initialize() {
        String email = User.normalizeEmail(properties.email());
        if (userAccountRepository.findOne(UserSpecifications.hasEmail(email)).isPresent()) {
            return;
        }
        User admin = userAccountRepository.save(
                new User(email, passwordEncoder.encode(properties.password()), Instant.now(),
                        new Address("Av Mexico", "", "Jalisco", "Jalisco", "45645", "MX"))
        );

        userRoleRepository.saveAll(List.of(new UserRole(admin, Role.ADMIN), new UserRole(admin, Role.CUSTOMER)));
    }
}
