package com.josue.ecommerce.identity;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class DemoCurrentUserProvider implements CurrentUserProvider {

    private static final UUID DEMO_USER_ID = UUID.fromString("02a0d352-7d58-4313-b4a1-4b13423d15bb");

    @Override
    public UUID demoPrincipalUserId() {
        return DEMO_USER_ID;
    }
}
