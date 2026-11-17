package com.example.inspection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.inspection.dto.ReportDtos.AutoPayload;
import com.example.inspection.entity.InspectionReport;
import com.example.inspection.repository.InspectionReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final InspectionReportRepository reportRepo;
    private final StorageService storageService;
    private final PdfService pdfService;
    private final ScoreService scoreService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void generate(Long reportId) {
        try {
            InspectionReport report = reportRepo.findById(reportId).orElseThrow();
            AutoPayload a = objectMapper.readValue(report.getRawAutoJson(), AutoPayload.class);
            @SuppressWarnings("unchecked")
            Map<String, String> manual = objectMapper.readValue(report.getManualJson(), Map.class);
            ScoreService.ScoreResult sr = scoreService.score(a, manual);

            try (InputStream in = pdfService.build(
                    report.getReportNo(),
                    report.getOverallScore(),
                    report.getVerdict(),
                    report.getSummary(),
                    manual,
                    sr.warnings())) {
                byte[] bytes = in.readAllBytes();
                String key = "reports/" + report.getReportNo() + ".pdf";
                storageService.store(key, new ByteArrayInputStream(bytes));
                report.setPdfObjectKey(key);
                report.setPdfSha256(sha256(bytes));
                reportRepo.save(report);
            }
        } catch (Exception e) {
            log.error("PDF generation failed for report {}", reportId, e);
        }
    }

    private String sha256(byte[] data) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}