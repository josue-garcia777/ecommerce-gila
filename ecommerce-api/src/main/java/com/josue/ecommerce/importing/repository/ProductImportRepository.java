package com.josue.ecommerce.importing.repository;

import com.josue.ecommerce.importing.domain.ProductImport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImportRepository extends JpaRepository<ProductImport, UUID> {

    @Query("select p.id as id, p.filename as filename, p.status as status, p.createdCount as createdCount, "
            + "p.updatedCount as updatedCount, p.rejectedCount as rejectedCount, p.submittedAt as submittedAt, "
            + "p.completedAt as completedAt from ProductImport p where p.id = :id")
    Optional<ImportMetadata> findMetadataById(@Param("id") UUID id);
}
