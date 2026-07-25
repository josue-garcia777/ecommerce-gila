package com.josue.ecommerce.identity.service;

import org.springframework.transaction.annotation.Transactional;

public interface InitAdminService {
    @Transactional
    void initialize();
}
