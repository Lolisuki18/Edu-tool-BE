package com.edutool.service;

import com.edutool.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    /**
     * Export users to CSV format
     * @param users - List of users to export
     * @return CSV content as byte array
     */
    public ByteArrayInputStream exportUsersToCsv(List<User> users) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            // Write CSV header
            writer.println("User ID,Username,Email,Full Name,Role,Status,Created At");

            // Write user data
            for (User user : users) {
                writer.println(String.format("%d,%s,%s,%s,%s,%s,%s",
                        user.getUserId(),
                        escapeCsv(user.getUsername()),
                        escapeCsv(user.getEmail()),
                        escapeCsv(user.getFullName()),
                        user.getRole().toString(),
                        user.getStatus().toString(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""));
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export users to CSV", e);
        }
    }

    /**
     * Escape CSV special characters
     * @param value - String value to escape
     * @return Escaped string
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
