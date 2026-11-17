package com.example.inspection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.inspection.dto.ReportDtos.*;
import com.example.inspection.entity.Device;
import com.example.inspection.entity.InspectionReport;
import com.example.inspection.exception.ApiException;
import com.example.inspection.repository.DeviceRepository;
import com.example.inspection.repository.InspectionReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inspection.service.ReportService;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DeviceRepository deviceRepo;
    private final InspectionReportRepository reportRepo;
    private final StorageService storageService;
    private final ScoreService scoreService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ReportSubmitResponse submit(Long userId, ReportSubmitRequest req) throws Exception {
        AutoPayload a = req.autoPayload();

        Device device = new Device();
        device.setUserId(userId);
        device.setManufacturer(a.manufacturer());
        device.setModel(a.model());
        device.setFingerprint(a.fingerprint());
        device.setAndroidVersion(a.androidVersion());
        device.setSdkInt(a.sdkInt());
        deviceRepo.save(device);

        ScoreService.ScoreResult sr = scoreService.score(a, req.manualResults());

        InspectionReport report = new InspectionReport();
        report.setUserId(userId);
        report.setDeviceId(device.getId());
        report.setReportNo("INS-" + Instant.now().toEpochMilli() + "-" + (1000 + new Random().nextInt(9000)));
        report.setOverallScore(sr.score());
        report.setVerdict(sr.verdict());
        report.setSummary(sr.summary());
        report.setRawAutoJson(objectMapper.writeValueAsString(a));
        report.setManualJson(objectMapper.writeValueAsString(
                req.manualResults() == null ? Map.of() : req.manualResults()));
        report.setCreatedAt(Instant.now());
        reportRepo.save(report);

        // 关键：通过独立 Bean 调用，让 @Async 代理生效
        pdfGenerationService.generate(report.getId());

        return new ReportSubmitResponse(report.getId(), report.getReportNo(), sr.score(), sr.verdict(),
                sr.warnings(), report.getCreatedAt());
    }

    public PageResponse<ReportMetaResponse> list(Long userId, int page, int size) {
        var p = reportRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        List<ReportMetaResponse> content = p.getContent().stream()
                .map(r -> new ReportMetaResponse(r.getId(), r.getReportNo(), r.getOverallScore(),
                        r.getVerdict(), r.getSummary(), r.getCreatedAt(), r.getPdfObjectKey() != null))
                .toList();
        return new PageResponse<>(content, p.getTotalElements(), p.getTotalPages(), page, size);
    }

    public PdfFile loadPdf(Long reportId, Long userId) {
        InspectionReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "report not found"));
        if (!Objects.equals(r.getUserId(), userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "not your report");
        }
        if (r.getPdfObjectKey() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "PDF_NOT_READY", "pdf still generating");
        }
        return new PdfFile(r.getReportNo() + ".pdf", storageService.load(r.getPdfObjectKey()));
    }

    public record PdfFile(String fileName, InputStream stream) {}
}