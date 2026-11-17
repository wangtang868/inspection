package com.example.inspection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "inspection_report")
@Getter @Setter
public class InspectionReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "device_id", nullable = false) private Long deviceId;
    @Column(name = "report_no", nullable = false, unique = true) private String reportNo;
    @Column(name = "overall_score") private Integer overallScore;
    private String verdict;
    private String summary;
    @Column(name = "raw_auto_json", columnDefinition = "LONGTEXT") private String rawAutoJson;
    @Column(name = "manual_json", columnDefinition = "LONGTEXT") private String manualJson;
    @Column(name = "pdf_object_key") private String pdfObjectKey;
    @Column(name = "pdf_sha256") private String pdfSha256;
    @Column(name = "created_at") private Instant createdAt = Instant.now();
}