package com.josue.ecommerce.identity.repository;

import com.josue.ecommerce.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
}
