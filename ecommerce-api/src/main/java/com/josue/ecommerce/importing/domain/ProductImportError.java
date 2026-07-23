package com.josue.ecommerce.importing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "product_import_errors")
public class ProductImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_id", nullable = false)
    private ProductImport productImport;

    @Getter
    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Getter
    @Column()
    private String sku;

    @Getter
    @Column(nullable = false)
    private String reason;

    protected ProductImportError() {
    }

    ProductImportError(ProductImport productImport, int rowNumber, String sku, String reason) {
        if (productImport == null) {
            throw new IllegalArgumentException("Product import is required");
        }
        this.productImport = productImport;
        this.rowNumber = rowNumber;
        this.sku = sku;
        this.reason = reason;
    }

}
