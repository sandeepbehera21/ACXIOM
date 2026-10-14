package com.acxiom.crm.entity;

/**
 * Opportunity pipeline stages, exactly as enumerated in the specification
 * (p.5 §4.8): "Qualification, Proposal, Negotiation, Won and Lost".
 * The same five values are required by the p.17 §17.12 pipeline chart.
 */
public enum OpportunityStage {
    QUALIFICATION,
    PROPOSAL,
    NEGOTIATION,
    WON,
    LOST
}
