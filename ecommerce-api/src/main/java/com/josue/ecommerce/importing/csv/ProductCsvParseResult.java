package com.josue.ecommerce.importing.csv;

import com.josue.ecommerce.product.service.cmd.ProductImportCommand;
import java.util.List;

public record ProductCsvParseResult(
        List<ProductImportCommand> acceptedRows,
        List<RejectedCsvRow> rejectedRows
) {
}
