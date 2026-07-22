package com.josue.ecommerce.importing.csv;

import com.josue.ecommerce.product.domain.Sku;
import com.josue.ecommerce.product.service.cmd.ProductImportCommand;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class ProductCsvParser {

    private static final Pattern DECIMAL = Pattern.compile("-?[0-9]+(?:\\.[0-9]+)?");
    private static final Pattern INTEGER = Pattern.compile("-?[0-9]+");

    public ProductCsvParseResult parse(byte[] content) throws IOException {
        List<RawRow> rows = readRows(content);

        Map<String, Integer> skuCounts = new HashMap<>();

        for (RawRow row : rows) {
            String normalizedSku = normalizeSku(row.sku());
            if (normalizedSku != null) {
                skuCounts.merge(normalizedSku, 1, Integer::sum);
            }
        }

        List<ProductImportCommand> accepted = new ArrayList<>();
        List<RejectedCsvRow> rejected = new ArrayList<>();

        for (RawRow row : rows) {
            String normalizedSku = normalizeSku(row.sku());
            if (normalizedSku != null && skuCounts.get(normalizedSku) > 1) {
                rejected.add(new RejectedCsvRow(row.rowNumber(), normalizedSku, "DUPLICATE_SKU_IN_FILE"));
                continue;
            }
            Validation validation = validate(row, normalizedSku);
            if (!validation.errors().isEmpty()) {
                rejected.add(new RejectedCsvRow(row.rowNumber(), normalizedSku,
                        String.join("; ", validation.errors())));
            } else {
                accepted.add(new ProductImportCommand(
                        normalizedSku,
                        row.name(),
                        row.description(),
                        row.category(),
                        validation.price(),
                        validation.stock(),
                        validation.weightKg()
                ));
            }
        }
        return new ProductCsvParseResult(List.copyOf(accepted), List.copyOf(rejected));
    }

    private List<RawRow> readRows(byte[] content) throws IOException {
        try (InputStreamReader reader = CsvHeaderValidator.reader(content);
             CSVParser parser = CsvHeaderValidator.format().parse(reader)) {
            if (!parser.getHeaderNames().equals(CsvHeaderValidator.EXPECTED_HEADERS)) {
                throw CsvHeaderValidator.invalidHeaders();
            }
            List<RawRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(new RawRow(
                        Math.toIntExact(record.getRecordNumber() + 1),
                        value(record, 0), value(record, 1), value(record, 2), value(record, 3),
                        value(record, 4), value(record, 5), value(record, 6)
                ));
            }
            return rows;
        }
    }

    private String value(CSVRecord record, int index) {
        return index < record.size() ? record.get(index).trim() : "";
    }

    private String normalizeSku(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new Sku(value).value();
        } catch (IllegalArgumentException exception) {
            return value.trim().toUpperCase(java.util.Locale.ROOT);
        }
    }

    private Validation validate(RawRow row, String normalizedSku) {
        List<String> errors = new ArrayList<>();
        required(row.name(), "Name", 200, errors);
        required(row.sku(), "SKU", 64, errors);
        required(row.description(), "Description", 2000, errors);
        required(row.category(), "Category", 100, errors);
        BigDecimal price = decimal(row.price(), "Price", 2, 17, errors);
        Integer stock = integer(row.stock(), errors);
        BigDecimal weight = decimal(row.weightKg(), "Weight", 3, 7, errors);
        if (normalizedSku != null && normalizedSku.length() > 64 && errors.stream().noneMatch(e -> e.startsWith("SKU"))) {
            errors.add("SKU must not exceed 64 characters");
        }
        return new Validation(errors, price, stock, weight);
    }

    private void required(String value, String field, int maximumLength, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field + " is required");
        } else if (value.length() > maximumLength) {
            errors.add(field + " must not exceed " + maximumLength + " characters");
        }
    }

    private BigDecimal decimal(String value, String field, int maximumScale, int maximumIntegerDigits,
                               List<String> errors) {
        if (!DECIMAL.matcher(value).matches()) {
            errors.add(field + " must be a decimal number");
            return null;
        }
        BigDecimal decimal = new BigDecimal(value);
        if (decimal.signum() < 0) {
            errors.add(field + " must not be negative");
        }
        if (decimal.scale() > maximumScale) {
            errors.add(field + " must have at most " + maximumScale + " decimal places");
        }
        if (Math.max(0, decimal.precision() - decimal.scale()) > maximumIntegerDigits) {
            errors.add(field + " is out of range");
        }
        return decimal;
    }

    private Integer integer(String value, List<String> errors) {
        if (!INTEGER.matcher(value).matches()) {
            errors.add("Stock must be an integer");
            return null;
        }
        try {
            int stock = Integer.parseInt(value);
            if (stock < 0) {
                errors.add("Stock must not be negative");
            }
            return stock;
        } catch (NumberFormatException exception) {
            errors.add("Stock is out of range");
            return null;
        }
    }

    private record RawRow(int rowNumber, String name, String sku, String description, String category,
                          String price, String stock, String weightKg) {
    }

    private record Validation(List<String> errors, BigDecimal price, Integer stock, BigDecimal weightKg) {
    }
}
