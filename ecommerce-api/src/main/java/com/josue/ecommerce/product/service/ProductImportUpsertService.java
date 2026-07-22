package com.josue.ecommerce.product.service;

import com.josue.ecommerce.product.service.cmd.ProductImportCommand;
import com.josue.ecommerce.product.service.cmd.ProductImportResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface ProductImportUpsertService {
    @Transactional
    ProductImportResult upsert(Collection<ProductImportCommand> commands);
}
