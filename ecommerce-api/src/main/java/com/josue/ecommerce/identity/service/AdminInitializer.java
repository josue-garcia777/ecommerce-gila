package com.josue.ecommerce.identity.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final InitAdminService initAdminService;

    public AdminInitializer(InitAdminService initAdminService) {
        this.initAdminService = initAdminService;
    }

    @Override
    public void run(ApplicationArguments args) {
        initAdminService.initialize();
    }
}
