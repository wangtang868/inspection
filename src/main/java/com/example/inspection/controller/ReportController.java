package com.example.inspection.controller;

import com.example.inspection.dto.ReportDtos.*;
import com.example.inspection.security.AppUserDetails;
import com.example.inspection.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportSubmitResponse submit(@AuthenticationPrincipal AppUserDetails user,
                                       @Valid @RequestBody ReportSubmitRequest req) throws Exception {
        return reportService.submit(user.getId(), req);
    }

    @GetMapping
    public PageResponse<ReportMetaResponse> list(@AuthenticationPrincipal AppUserDetails user,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return reportService.list(user.getId(), page, size);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal AppUserDetails user,
                                                        @PathVariable Long id) {
        var file = reportService.loadPdf(id, user.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8).build());
        headers.setCacheControl("no-store");
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(file.stream()));
    }
}
