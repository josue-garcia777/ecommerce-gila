package com.josue.ecommerce.importing.csv;

import com.josue.ecommerce.shared.error.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.josue.ecommerce.shared.error.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CsvHeaderValidator {

    public static final List<String> EXPECTED_HEADERS = List.of(
            "name", "sku", "description", "category", "price", "stock", "weight_kg"
    );

    public void validate(byte[] content) {
        try (InputStreamReader reader = reader(content);
             CSVParser parser = format().parse(reader)) {
            if (!parser.getHeaderNames().equals(EXPECTED_HEADERS)) {
                throw invalidHeaders();
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new BadRequestException(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid CSV structure",
                    "The file must be valid UTF-8 CSV with the exact required headers");
        }
    }

    static CSVFormat format() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(false)
                .get();
    }

    static InputStreamReader reader(byte[] content) {
        return new InputStreamReader(
                new ByteArrayInputStream(content),
                StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
        );
    }

    static IllegalArgumentException invalidHeaders() {
        return new IllegalArgumentException("CSV headers must exactly match " + String.join(",", EXPECTED_HEADERS));
    }
}
