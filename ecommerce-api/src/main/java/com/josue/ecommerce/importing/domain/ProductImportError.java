package com.josue.ecommerce.importing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "product_import_errors")
public class ProductImportError {

    @Id
    private UUID id;

    @Column(name = "import_id", nullable = false)
    private UUID importId;

    @Getter
    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Getter
    @Column(length = 64)
    private String sku;

    @Getter
    @Column(nullable = false, length = 1000)
    private String reason;

    protected ProductImportError() {
    }

    public ProductImportError(UUID id, UUID importId, int rowNumber, String sku, String reason) {
        this.id = id;
        this.importId = importId;
        this.rowNumber = rowNumber;
        this.sku = sku;
        this.reason = reason;
    }

}
