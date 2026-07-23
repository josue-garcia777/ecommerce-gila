package com.josue.ecommerce.importing.service;

import java.util.UUID;

public interface ProductImportStart {
    void process(UUID importId);
}
