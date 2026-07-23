package com.josue.ecommerce.importing.service;

import com.josue.ecommerce.importing.dto.ImportStatusResponse;
import com.josue.ecommerce.importing.service.cmd.ImportWorkItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProductImportService {
    ImportWorkItem findImportItem(UUID importId);

    ImportStatusResponse getImportStatus(UUID importId);
}
