package com.acxiom.crm.controller;

import com.acxiom.crm.dto.PipelineReportResponse;
import com.acxiom.crm.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting endpoints (p.10 §11).
 * <p>
 * Scoping by ownership happens inside the service, so the same endpoint returns
 * the whole pipeline for an Admin or Manager and only the caller's own figures for
 * a Sales Executive.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Headline figures plus the weighted, stage-by-stage pipeline. */
    @GetMapping("/pipeline")
    public PipelineReportResponse pipeline() {
        return reportService.pipeline();
    }
}
