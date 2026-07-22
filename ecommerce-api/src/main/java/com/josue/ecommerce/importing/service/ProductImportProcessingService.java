package com.josue.ecommerce.importing.service;

import java.util.UUID;

public interface ProductImportProcessingService {
    void process(UUID importId);
}
