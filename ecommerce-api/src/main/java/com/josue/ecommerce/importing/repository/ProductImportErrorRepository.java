package com.josue.ecommerce.importing.repository;

import com.josue.ecommerce.importing.domain.ProductImportError;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImportErrorRepository extends JpaRepository<ProductImportError, UUID> {

    List<ProductImportError> findAllByImportIdOrderByRowNumber(UUID importId);
}
