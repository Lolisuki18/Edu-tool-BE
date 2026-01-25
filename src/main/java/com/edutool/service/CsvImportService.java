package com.edutool.service;

import com.edutool.dto.request.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final UserService userService;

    /**
     * Import users from CSV file
     * @param file - CSV file
     * @return Import result with success and error counts
     */
    public ImportResult importUsersFromCsv(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file (*.csv)");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] fields = parseCsvLine(line);

                    // Validate field count
                    if (fields.length < 5) {
                        errors.add("Line " + lineNumber + ": Invalid format - Expected at least 5 fields (Username, Password, Email, Full Name, Role, Status)");
                        errorCount++;
                        continue;
                    }

                    // Validate required fields are not empty
                    for (int i = 0; i < 5; i++) {
                        if (fields[i].trim().isEmpty()) {
                            errors.add("Line " + lineNumber + ": Field " + (i + 1) + " is empty");
                            errorCount++;
                            continue;
                        }
                    }

                    CreateUserRequest request = new CreateUserRequest();
                    request.setUsername(fields[0].trim());
                    request.setPassword(fields[1].trim());
                    request.setEmail(fields[2].trim());
                    request.setFullName(fields[3].trim());
                    request.setRole(fields[4].trim());
                    request.setStatus(fields.length > 5 && !fields[5].trim().isEmpty() ? fields[5].trim() : "ACTIVE");

                    userService.createUser(request);
                    successCount++;

                } catch (IllegalArgumentException e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": Error - " + e.getMessage());
                    errorCount++;
                    System.err.println("Error importing line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import users from CSV: " + e.getMessage(), e);
        }

        return new ImportResult(successCount, errorCount, errors);
    }

    /**
     * Parse CSV line handling quoted fields
     * @param line - CSV line
     * @return Array of field values
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Import result class
     */
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> errors;

        public ImportResult(int successCount, int errorCount, List<String> errors) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
