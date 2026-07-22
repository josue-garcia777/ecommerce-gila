package com.josue.ecommerce.importing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_imports")
public class ProductImport {

    @Getter
    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    @Getter
    private String filename;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "file_content")
    private byte[] fileContent;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ImportStatus status;

    @Getter
    @Column(name = "created_count", nullable = false)
    private int createdCount;

    @Getter
    @Column(name = "updated_count", nullable = false)
    private int updatedCount;

    @Getter
    @Column(name = "rejected_count", nullable = false)
    private int rejectedCount;

    @Getter
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Getter
    @Column(name = "completed_at")
    private Instant completedAt;

    protected ProductImport() {
    }

    public ProductImport(UUID id, String filename, byte[] fileContent, Instant submittedAt) {
        this.id = id;
        this.filename = filename;
        this.fileContent = fileContent.clone();
        this.status = ImportStatus.PENDING;
        this.submittedAt = submittedAt;
    }

    public void start() {
        if (status != ImportStatus.PENDING) {
            throw new IllegalStateException("Only a pending import can start");
        }
        status = ImportStatus.PROCESSING;
    }

    public void complete(int created, int updated, int rejected, Instant now) {
        if (status != ImportStatus.PROCESSING) {
            throw new IllegalStateException("Only a processing import can complete");
        }
        createdCount = created;
        updatedCount = updated;
        rejectedCount = rejected;
        status = rejected == 0 ? ImportStatus.COMPLETED : ImportStatus.COMPLETED_WITH_ERRORS;
        fileContent = null;
        completedAt = now;
    }

    public void fail(Instant now) {
        if (status.terminal()) {
            return;
        }
        status = ImportStatus.FAILED;
        fileContent = null;
        completedAt = now;
    }


    public byte[] getFileContent() {
        return fileContent == null ? null : fileContent.clone();
    }
}
