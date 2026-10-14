package com.acxiom.crm.dto;

/** Result of a lead conversion: the new customer and, optionally, an opportunity. */
public record LeadConversionResponse(
        CustomerResponse customer,
        OpportunityResponse opportunity,
        LeadResponse lead) {
}
