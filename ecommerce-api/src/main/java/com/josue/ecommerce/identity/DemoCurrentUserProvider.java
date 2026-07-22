package com.josue.ecommerce.identity;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class DemoCurrentUserProvider implements CurrentUserProvider {

    private static final UUID DEMO_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public UUID demoPrincipalUserId() {
        return DEMO_USER_ID;
    }
}
