package com.josue.ecommerce.importing.dto;

import com.josue.ecommerce.importing.domain.ImportStatus;
import java.time.Instant;
import java.util.UUID;

public record ImportSubmissionResponse(
        UUID importId,
        ImportStatus status,
        Instant submittedAt,
        String statusUrl
) {
}
