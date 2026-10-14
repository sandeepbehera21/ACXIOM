package com.acxiom.crm.service;

import com.acxiom.crm.dto.PipelineReportResponse;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.entity.LeadStatus;
import com.acxiom.crm.entity.Opportunity;
import com.acxiom.crm.entity.OpportunityStage;
import com.acxiom.crm.entity.OpportunityStatus;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import com.acxiom.crm.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pipeline and dashboard figures (p.4-5 §4.8, p.10 §11, p.17 §17.11, §17.12).
 */
@Service
public class ReportService {

    private static final List<LeadStatus> CLOSED_LEAD_STATUSES =
            List.of(LeadStatus.CONVERTED, LeadStatus.LOST, LeadStatus.UNQUALIFIED);

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final CurrentUserService currentUserService;

    public ReportService(OpportunityRepository opportunityRepository,
                         CustomerRepository customerRepository,
                         LeadRepository leadRepository,
                         CurrentUserService currentUserService) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PipelineReportResponse pipeline() {
        Long scopeOwnerId = currentUserService.canSeeAllRecords()
                ? null
                : currentUserService.requireCurrentUser().getId();

        long customerCount = customerRepository.count(
                CustomerRepository.filter(null, null, scopeOwnerId));

        List<Lead> leads = leadRepository.findAll(
                LeadRepository.filter(null, null, scopeOwnerId));
        long openLeadCount = leads.stream()
                .filter(lead -> !CLOSED_LEAD_STATUSES.contains(lead.getStatus()))
                .count();

        // Chart 1: Lead Status breakdown (§17.12: New, Contacted, Qualified, Lost, Converted)
        Map<String, Long> leadStatusCounts = new LinkedHashMap<>();
        for (LeadStatus ls : LeadStatus.values()) {
            leadStatusCounts.put(ls.name(), 0L);
        }
        for (Lead lead : leads) {
            if (lead.getStatus() != null) {
                leadStatusCounts.put(lead.getStatus().name(),
                        leadStatusCounts.getOrDefault(lead.getStatus().name(), 0L) + 1);
            }
        }

        List<Opportunity> opportunities = opportunityRepository.findAll(
                OpportunityRepository.filter(null, null, null, scopeOwnerId));

        long openCount = 0;
        long wonCount = 0;
        long lostCount = 0;
        BigDecimal totalOpenAmount = BigDecimal.ZERO;
        BigDecimal totalWeightedAmount = BigDecimal.ZERO;
        Map<OpportunityStage, List<Opportunity>> openByStage = new EnumMap<>(OpportunityStage.class);

        // Chart 2: Opportunity Pipeline stage counts (§17.12: Qualification, Proposal, Negotiation, Won, Lost)
        Map<String, Long> opportunityStageCounts = new LinkedHashMap<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            opportunityStageCounts.put(stage.name(), 0L);
        }

        for (Opportunity opportunity : opportunities) {
            if (opportunity.getStage() != null) {
                opportunityStageCounts.put(opportunity.getStage().name(),
                        opportunityStageCounts.getOrDefault(opportunity.getStage().name(), 0L) + 1);
            }

            switch (opportunity.getStatus()) {
                case WON -> wonCount++;
                case LOST -> lostCount++;
                case OPEN -> {
                    openCount++;
                    totalOpenAmount = totalOpenAmount.add(nullSafe(opportunity.getAmount()));
                    totalWeightedAmount = totalWeightedAmount.add(
                            nullSafe(opportunity.getWeightedAmount()));
                    openByStage
                            .computeIfAbsent(opportunity.getStage(), key -> new ArrayList<>())
                            .add(opportunity);
                }
            }
        }

        List<PipelineReportResponse.StageSummary> stageSummaries = new ArrayList<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            if (stage == OpportunityStage.WON || stage == OpportunityStage.LOST) {
                continue;
            }
            List<Opportunity> stageOpportunities = openByStage.getOrDefault(stage, List.of());
            BigDecimal stageAmount = BigDecimal.ZERO;
            BigDecimal stageWeighted = BigDecimal.ZERO;
            for (Opportunity opportunity : stageOpportunities) {
                stageAmount = stageAmount.add(nullSafe(opportunity.getAmount()));
                stageWeighted = stageWeighted.add(nullSafe(opportunity.getWeightedAmount()));
            }
            stageSummaries.add(new PipelineReportResponse.StageSummary(
                    stage.name(), stageOpportunities.size(), stageAmount, stageWeighted));
        }

        // Chart 3: Monthly Sales aggregation (§17.12) - last 6 months
        YearMonth currentYm = YearMonth.now();
        DateTimeFormatter ymFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<PipelineReportResponse.MonthlySalesSummary> monthlySales = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            BigDecimal monthTotal = BigDecimal.ZERO;

            for (Opportunity opp : opportunities) {
                if (opp.getStatus() == OpportunityStatus.WON || opp.getStage() == OpportunityStage.WON) {
                    YearMonth oppYm = opp.getExpectedCloseDate() != null
                            ? YearMonth.from(opp.getExpectedCloseDate())
                            : (opp.getModifiedDate() != null
                                ? YearMonth.from(opp.getModifiedDate())
                                : YearMonth.from(opp.getCreatedDate()));
                    if (ym.equals(oppYm)) {
                        monthTotal = monthTotal.add(nullSafe(opp.getAmount()));
                    }
                }
            }
            monthlySales.add(new PipelineReportResponse.MonthlySalesSummary(ym.format(ymFormatter), monthTotal));
        }

        return new PipelineReportResponse(
                customerCount,
                leads.size(),
                openLeadCount,
                opportunities.size(), // Total Opportunities count (Card 4)
                openCount,
                wonCount,
                lostCount,
                totalOpenAmount,
                totalWeightedAmount,
                stageSummaries,
                leadStatusCounts,
                opportunityStageCounts,
                monthlySales);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
