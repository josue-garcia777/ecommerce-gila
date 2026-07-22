package com.josue.ecommerce.importing.repository;

import com.josue.ecommerce.importing.domain.ImportStatus;
import java.time.Instant;
import java.util.UUID;

public interface ImportMetadata {

    UUID getId();

    String getFilename();

    ImportStatus getStatus();

    int getCreatedCount();

    int getUpdatedCount();

    int getRejectedCount();

    Instant getSubmittedAt();

    Instant getCompletedAt();
}
