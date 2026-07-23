package com.josue.ecommerce.importing.repository;

import com.josue.ecommerce.importing.domain.ProductImportError;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductImportErrorRepository extends JpaRepository<ProductImportError, UUID>,
        JpaSpecificationExecutor<ProductImportError> {
}
