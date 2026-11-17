package com.example.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ReportDtos {
    public record AutoPayload(
            @NotBlank String manufacturer,
            @NotBlank String model,
            @NotBlank String fingerprint,
            @NotBlank String androidVersion,
            @NotNull Integer sdkInt,
            Boolean emulatorLikely,
            Boolean suBinaryFound,
            Boolean testKeysBuild) {}

    public record ReportSubmitRequest(
            @NotNull AutoPayload autoPayload,
            Map<String, String> manualResults) {}

    public record ReportSubmitResponse(
            Long id, String reportNo, int score, String verdict,
            List<String> warnings, Instant createdAt) {}

    public record ReportMetaResponse(
            Long id, String reportNo, int score, String verdict,
            String summary, Instant createdAt, boolean pdfReady) {}

    public record PageResponse<T>(
            List<T> content, long totalElements, int totalPages, int page, int size) {}
}