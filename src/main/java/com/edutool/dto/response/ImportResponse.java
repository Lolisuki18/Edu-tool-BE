package com.edutool.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponse {
    private int successCount;
    private int errorCount;
    private int totalRows;
    private List<String> errors;
    private String message;

    public ImportResponse(int successCount, int errorCount, List<String> errors) {
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.totalRows = successCount + errorCount;
        this.errors = errors;
        this.message = String.format("Import completed: %d succeeded, %d failed", successCount, errorCount);
    }
}
