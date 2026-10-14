package com.acxiom.crm.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dashboard and pipeline figures covering all 8 mandatory KPI cards (§17.11)
 * and all 3 Chart.js charts (§17.12).
 */
public record PipelineReportResponse(
        long customerCount,
        long leadCount,
        long openLeadCount,
        long totalOpportunities,
        long openCount,
        long wonCount,
        long lostCount,
        BigDecimal totalOpenAmount,
        BigDecimal totalWeightedAmount,
        List<StageSummary> byStage,
        Map<String, Long> leadStatusCounts,
        Map<String, Long> opportunityStageCounts,
        List<MonthlySalesSummary> monthlySales) {

    /** One row of the stage-wise pipeline breakdown. */
    public record StageSummary(
            String stage,
            long count,
            BigDecimal totalAmount,
            BigDecimal weightedAmount) {
    }

    /** Monthly sales aggregation for Chart.js */
    public record MonthlySalesSummary(
            String month,
            BigDecimal totalSales) {
    }
}
