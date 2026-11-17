package com.example.inspection.repository;

import com.example.inspection.entity.InspectionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionReportRepository extends JpaRepository<InspectionReport, Long> {
    Page<InspectionReport> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}