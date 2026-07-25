package com.josue.ecommerce.identity;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID userPrincipalId();
}
