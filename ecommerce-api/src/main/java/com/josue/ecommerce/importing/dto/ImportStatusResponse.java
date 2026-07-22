package com.josue.ecommerce.importing.dto;

import com.josue.ecommerce.importing.domain.ImportStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ImportStatusResponse(
        UUID importId,
        ImportStatus status,
        String filename,
        ImportSummaryResponse summary,
        Instant submittedAt,
        Instant completedAt,
        List<RejectedRowResponse> rejectedRows
) {
}
