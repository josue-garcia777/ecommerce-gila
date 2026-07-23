package com.josue.ecommerce.importing.repository.specification;

import com.josue.ecommerce.importing.domain.ProductImportError;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ProductImportErrorSpecifications {

    private ProductImportErrorSpecifications() {
    }

    public static Specification<ProductImportError> forImport(UUID importId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("productImport").get("id"), importId);
    }
}
