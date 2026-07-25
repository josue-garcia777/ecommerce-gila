package com.josue.ecommerce.importing.csv;

import com.josue.ecommerce.importing.csv.impl.ProductCsvParseResult;

import java.io.IOException;

public interface ProductCsvParser {
    ProductCsvParseResult parse(byte[] content) throws IOException;
}
