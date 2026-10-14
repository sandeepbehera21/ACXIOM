package com.acxiom.crm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.entity.LeadStatus;
import com.acxiom.crm.repository.AuditLogRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Automated verification of the Phase 1 exit checklist.
 * <p>
 * These tests run against the real Spring context, the real Spring Security
 * filter chain and the real service layer - only the database is substituted
 * (in-memory H2 in MySQL mode, see src/test/resources/application-test.properties).
 * That means each rule below is exercised exactly as it would be in the running
 * application: the same validation, the same ownership checks, the same
 * transaction boundaries and the same audit writes.
 * <p>
 * CSRF is deliberately <i>not</i> disabled for these tests. The helper performs
 * the same two steps the React client performs - fetch the token, echo it in the
 * {@code X-XSRF-TOKEN} header - so the CSRF configuration itself is under test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Phase 1 exit checklist")
class Phase1ExitChecklistTest {

    private static final String ADMIN_EMAIL = "admin@acxiomcrm.local";
    private static final String ADMIN_PASSWORD = "Admin@123";

    /** Keeps each test's records distinct so duplicate-prevention rules do not collide. */
    private static final AtomicInteger SEQ = new AtomicInteger(1000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    private String csrfToken;
    private MockHttpSession session;

    // ------------------------------------------------------------- setup

    /**
     * The application must start and issue a CSRF token before anyone can log in,
     * so this also covers "both applications start" and "the SPA can obtain a
     * token while still anonymous".
     */
    @BeforeEach
    void bootstrapCsrfToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie)
                .as("the server must issue an XSRF-TOKEN cookie for the SPA to echo back")
                .isNotNull();
        this.csrfToken = cookie.getValue();
        this.session = null;
    }

    // ------------------------------------------------- 1. authentication

    @Test
    @DisplayName("seeded admin can log in and receives their role")
    void adminCanLogIn() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(get("/api/auth/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("wrong password is rejected without revealing whether the account exists")
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", ADMIN_EMAIL, "password", "definitely-wrong"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", "nobody@nowhere.local", "password", "whatever"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("failed logins are recorded in the audit trail")
    void failedLoginIsAudited() throws Exception {
        mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", ADMIN_EMAIL, "password", "definitely-wrong"))))
                .andExpect(status().isUnauthorized());

        assertThat(auditLogRepository.findAll())
                .as("a failed login must leave an audit record (p.5 §4.9)")
                .anyMatch(entry -> entry.getAction() == AuditAction.LOGIN_FAILURE);
    }

    @Test
    @DisplayName("the account locks temporarily after repeated failed attempts")
    void accountLocksAfterRepeatedFailures() throws Exception {
        String email = registerSalesExecutive();

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(authed(post("/api/auth/login"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(map("email", email, "password", "Wrong@123"))))
                    .andExpect(status().isUnauthorized());
        }

        // Even the correct password must now be refused while the lock holds.
        mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", email, "password", "Valid@123"))))
                .andExpect(status().isUnauthorized());

        assertThat(auditLogRepository.findAll())
                .as("the lockout must be audited")
                .anyMatch(entry -> entry.getAction() == AuditAction.LOCKOUT);
    }

    @Test
    @DisplayName("registration always creates a Sales Executive, never an Admin")
    void registrationCannotEscalateRole() throws Exception {
        String email = unique("escalate");

        mockMvc.perform(authed(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "name", "Would Be Admin",
                                "email", email,
                                "password", "Valid@123",
                                "confirmPassword", "Valid@123",
                                // An extra role field must be ignored: the DTO has no such
                                // property, so this cannot promote the account.
                                "role", "ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SALES_EXECUTIVE"));
    }

    @Test
    @DisplayName("registration enforces the password policy")
    void registrationEnforcesPasswordPolicy() throws Exception {
        mockMvc.perform(authed(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "name", "Weak Password",
                                "email", unique("weak"),
                                "password", "alllowercase",
                                "confirmPassword", "alllowercase"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("unauthenticated requests are refused with 401")
    void unauthenticatedRequestIsRefused() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------- 2. authorization by role

    @Test
    @DisplayName("a Sales Executive is refused the audit log with 403")
    void salesExecutiveCannotReadAuditLogs() throws Exception {
        loginAsNewSalesExecutive();

        mockMvc.perform(authed(get("/api/audit-logs")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a Sales Executive is refused the user list with 403")
    void salesExecutiveCannotListUsers() throws Exception {
        loginAsNewSalesExecutive();

        mockMvc.perform(authed(get("/api/users")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an Admin can read the audit log")
    void adminCanReadAuditLogs() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(get("/api/audit-logs")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ------------------------------------------------------ 3. customers

    @Test
    @DisplayName("a customer can be created, read, updated, searched and deleted")
    void customerCrudLifecycle() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        String email = unique("lifecycle");
        String phone = uniquePhone();

        MvcResult created = mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Lifecycle Ltd", email, phone))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerCode").value(org.hamcrest.Matchers.startsWith("CUS-")))
                .andExpect(jsonPath("$.status").value("PROSPECT"))
                .andReturn();

        long id = readId(created);

        mockMvc.perform(authed(get("/api/customers/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(authed(put("/api/customers/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Lifecycle Ltd (renamed)", email, phone,
                                "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lifecycle Ltd (renamed)"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Search must find the record by name.
        mockMvc.perform(authed(get("/api/customers").param("search", "renamed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        // Filter by status.
        mockMvc.perform(authed(get("/api/customers").param("status", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").exists());

        mockMvc.perform(authed(delete("/api/customers/" + id)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authed(get("/api/customers/" + id)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a duplicate customer email is rejected with 409")
    void duplicateCustomerEmailIsRejected() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = unique("duplicate");

        mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("First", email, uniquePhone()))))
                .andExpect(status().isCreated());

        mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Second", email, uniquePhone()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("a duplicate customer phone is rejected with 409")
    void duplicateCustomerPhoneIsRejected() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String phone = uniquePhone();

        mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("First", unique("dupphone"), phone))))
                .andExpect(status().isCreated());

        mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Second", unique("dupphone"), phone))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    @DisplayName("invalid customer fields return per-field errors")
    void customerValidationIsReportedPerField() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "name", "",
                                "email", "not-an-email",
                                "phone", "12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    @DisplayName("a Sales Executive only sees their own customers")
    void salesExecutiveOnlySeesOwnCustomers() throws Exception {
        // An Admin creates a customer owned by the Admin.
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String adminEmail = unique("admin-owned");
        MvcResult adminCustomer = mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Admin Owned", adminEmail, uniquePhone()))))
                .andExpect(status().isCreated())
                .andReturn();
        long adminCustomerId = readId(adminCustomer);

        // Now a Sales Executive looks for it.
        loginAsNewSalesExecutive();

        mockMvc.perform(authed(get("/api/customers/" + adminCustomerId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(authed(get("/api/customers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + adminCustomerId + ")]").doesNotExist());
    }

    // ----------------------------------------------------------- 4. leads

    @Test
    @DisplayName("a lead can be created, updated and searched")
    void leadCrudLifecycle() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        MvcResult created = mockMvc.perform(authed(post("/api/leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(leadBody("Lead Alpha", LeadStatus.NEW, unique("lead")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leadCode").value(org.hamcrest.Matchers.startsWith("LED-")))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        long id = readId(created);

        mockMvc.perform(authed(put("/api/leads/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(leadBody("Lead Alpha", LeadStatus.QUALIFIED, unique("lead")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUALIFIED"));

        mockMvc.perform(authed(get("/api/leads").param("status", "QUALIFIED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").exists());

        mockMvc.perform(authed(get("/api/leads").param("search", "Lead Alpha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    @DisplayName("an invalid lead status is rejected")
    void invalidLeadStatusIsRejected() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        Map<String, Object> body = leadBody("Bad Status", LeadStatus.NEW, unique("badstatus"));
        body.put("status", "NOT_A_REAL_STATUS");

        mockMvc.perform(authed(post("/api/leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------- 5. conversion

    @Test
    @DisplayName("a qualified lead converts into a customer and an opportunity")
    void qualifiedLeadConverts() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        long leadId = createLead(LeadStatus.QUALIFIED, unique("convert"));

        MvcResult converted = mockMvc.perform(
                        authed(post("/api/leads/" + leadId + "/convert"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(map(
                                        "createOpportunity", true,
                                        "opportunityName", "Converted Opportunity",
                                        "amount", 200000,
                                        "probability", 40,
                                        "expectedCloseDate", LocalDate.now().plusDays(30).toString(),
                                        "stage", "PROPOSAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").exists())
                .andExpect(jsonPath("$.opportunity.id").exists())
                .andExpect(jsonPath("$.lead.status").value("CONVERTED"))
                .andReturn();

        // Weighted pipeline: Amount x Probability / 100 = 200000 x 40 / 100.
        assertThat(readJson(converted).path("opportunity").path("weightedAmount").decimalValue())
                .as("weighted amount must be amount x probability / 100")
                .isEqualByComparingTo(new BigDecimal("80000.00"));

        Lead lead = leadRepository.findById(leadId).orElseThrow();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CONVERTED);
        assertThat(lead.getConvertedCustomer()).isNotNull();
        assertThat(lead.getConvertedDate()).isNotNull();

        // The opportunity must be persisted, linked to the converted lead and owned
        // by the customer the conversion created.
        assertThat(opportunityRepository.findAll())
                .as("the conversion must persist an opportunity linked to the lead")
                .anyMatch(opportunity -> opportunity.getLead() != null
                        && opportunity.getLead().getId().equals(leadId)
                        && opportunity.getCustomer() != null
                        && opportunity.getCustomer().getId().equals(lead.getConvertedCustomer().getId()));

        // The conversion is audited.
        assertThat(auditLogRepository.findAll())
                .anyMatch(entry -> entry.getAction() == AuditAction.CONVERT);
    }

    @Test
    @DisplayName("a lead cannot be converted twice")
    void leadCannotBeConvertedTwice() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        long leadId = createLead(LeadStatus.QUALIFIED, unique("twice"));

        mockMvc.perform(authed(post("/api/leads/" + leadId + "/convert"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("createOpportunity", false))))
                .andExpect(status().isOk());

        mockMvc.perform(authed(post("/api/leads/" + leadId + "/convert"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("createOpportunity", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    @DisplayName("only a QUALIFIED lead can be converted")
    void unqualifiedLeadCannotBeConverted() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        long leadId = createLead(LeadStatus.NEW, unique("unqualified"));

        mockMvc.perform(authed(post("/api/leads/" + leadId + "/convert"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("createOpportunity", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    /**
     * The transaction test (p.19 §17.19 step 12): when the opportunity half of a
     * conversion is rejected, the customer created moments earlier must not
     * survive. A customer left behind for a conversion that failed is exactly the
     * half-finished state the transaction requirement exists to prevent.
     */
    @Test
    @DisplayName("a failed conversion leaves no customer behind")
    void failedConversionRollsBackTheCustomer() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        String email = unique("rollback");
        long leadId = createLead(LeadStatus.QUALIFIED, email);

        long customersBefore = customerRepository.count();

        // Amount 0 violates the opportunity rule, so the whole conversion fails.
        mockMvc.perform(authed(post("/api/leads/" + leadId + "/convert"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "createOpportunity", true,
                                "opportunityName", "Should Not Exist",
                                "amount", 0,
                                "probability", 50,
                                "expectedCloseDate", LocalDate.now().plusDays(10).toString(),
                                "stage", "QUALIFICATION"))))
                .andExpect(status().isBadRequest());

        assertThat(customerRepository.count())
                .as("the customer created during the failed conversion must be rolled back")
                .isEqualTo(customersBefore);
        assertThat(customerRepository.findByEmailIgnoreCase(email))
                .as("no orphan customer may remain")
                .isEmpty();
        assertThat(leadRepository.findById(leadId).orElseThrow().getStatus())
                .as("the lead must not be marked converted")
                .isNotEqualTo(LeadStatus.CONVERTED);
    }

    // ---------------------------------------------------- 6. opportunities

    @Test
    @DisplayName("an opportunity amount of 0 is rejected")
    void opportunityAmountMustBePositive() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 0, 50, LocalDate.now().plusDays(10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    @DisplayName("a probability above 100 is rejected")
    void opportunityProbabilityMustBeWithinRange() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 50000, 101, LocalDate.now().plusDays(10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.probability").exists());
    }

    @Test
    @DisplayName("a past close date is rejected for an active opportunity")
    void opportunityCloseDateCannotBeInThePast() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 50000, 50, LocalDate.now().minusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.expectedCloseDate").exists());
    }

    @Test
    @DisplayName("an opportunity is created with a weighted amount")
    void opportunityIsCreatedWithWeightedAmount() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 100000, 25, LocalDate.now().plusDays(20)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stage").value("QUALIFICATION"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.weightedAmount").value(25000.00));
    }

    @Test
    @DisplayName("a WON stage records the deal as won with full probability")
    void wonStageDerivesStatusAndProbability() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        Map<String, Object> body = opportunityBody(customerId, 100000, 25, LocalDate.now().plusDays(20));
        body.put("stage", "WON");

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                // A won deal cannot still be 25% likely, and its stage and status
                // must not contradict each other.
                .andExpect(jsonPath("$.status").value("WON"))
                .andExpect(jsonPath("$.probability").value(100))
                .andExpect(jsonPath("$.weightedAmount").value(100000.00));
    }

    @Test
    @DisplayName("a customer with opportunities cannot be deleted")
    void customerWithOpportunitiesCannotBeDeleted() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 100000, 25, LocalDate.now().plusDays(20)))))
                .andExpect(status().isCreated());

        mockMvc.perform(authed(delete("/api/customers/" + customerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").exists());
    }

    // -------------------------------------------------------- 7. reporting

    @Test
    @DisplayName("the pipeline report returns weighted totals by stage")
    void pipelineReportIsWeighted() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        long customerId = createCustomer();

        mockMvc.perform(authed(post("/api/opportunities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(opportunityBody(customerId, 400000, 50, LocalDate.now().plusDays(20)))))
                .andExpect(status().isCreated());

        mockMvc.perform(authed(get("/api/reports/pipeline")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCount").isNumber())
                .andExpect(jsonPath("$.byStage").isArray())
                .andExpect(jsonPath("$.totalWeightedAmount").isNumber())
                .andExpect(jsonPath("$.byStage[?(@.stage == 'QUALIFICATION')]").exists());
    }

    // ----------------------------------------------------------- 8. users

    @Test
    @DisplayName("an Admin can list users for the assignment dropdowns")
    void adminCanListUsers() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(get("/api/users")))
                .andExpect(status().isOk())
                // The user DTO must never carry the password hash (p.15 §17.4).
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].role").exists());
    }

    @Test
    @DisplayName("logout ends the session")
    void logoutEndsTheSession() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(post("/api/auth/logout")))
                .andExpect(status().isNoContent());

        // The session was invalidated, so the same session object is now anonymous.
        MockHttpSession afterLogout = session;
        mockMvc.perform(get("/api/auth/me").session(afterLogout))
                .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------------------------- helpers

    /** Mirrors the React client: CSRF cookie plus matching header, and the session. */
    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder) {
        builder.cookie(new Cookie("XSRF-TOKEN", csrfToken))
                .header("X-XSRF-TOKEN", csrfToken);
        if (session != null) {
            builder.session(session);
        }
        return builder;
    }

    private void login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).as("login must establish a session").isNotNull();
    }

    /** Registers a fresh Sales Executive and logs in as them. */
    private void loginAsNewSalesExecutive() throws Exception {
        String email = registerSalesExecutive();
        // Registration does not sign the user in, so log in explicitly.
        session = null;
        login(email, "Valid@123");
    }

    private String registerSalesExecutive() throws Exception {
        String email = unique("sales");
        mockMvc.perform(authed(post("/api/auth/register"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "name", "Test Sales Executive",
                                "email", email,
                                "password", "Valid@123",
                                "confirmPassword", "Valid@123"))))
                .andExpect(status().isCreated());
        return email;
    }

    private long createCustomer() throws Exception {
        MvcResult result = mockMvc.perform(authed(post("/api/customers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customerBody("Opportunity Customer", unique("opp"), uniquePhone()))))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long createLead(LeadStatus status, String email) throws Exception {
        MvcResult result = mockMvc.perform(authed(post("/api/leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(leadBody("Convertible Lead", status, email))))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private Map<String, Object> customerBody(String name, String email, String phone,
                                             Object... extra) {
        Map<String, Object> body = map(
                "name", name,
                "email", email,
                "phone", phone,
                "companyName", "Example Industries",
                "city", "Pune",
                "state", "Maharashtra");
        for (int i = 0; i < extra.length; i += 2) {
            body.put((String) extra[i], extra[i + 1]);
        }
        return body;
    }

    private Map<String, Object> leadBody(String name, LeadStatus status, String email) {
        return map(
                "name", name,
                "email", email,
                // Valid per the phone rule: 10 digits beginning 6-9.
                "phone", uniquePhone(),
                "companyName", "Example Industries",
                "source", "Website",
                "status", status.name(),
                "priority", "MEDIUM",
                "expectedValue", 150000);
    }

    private Map<String, Object> opportunityBody(long customerId, Object amount,
                                                Object probability, LocalDate closeDate) {
        return map(
                "name", "Opportunity " + SEQ.incrementAndGet(),
                "customerId", customerId,
                "amount", amount,
                "stage", "QUALIFICATION",
                "probability", probability,
                "expectedCloseDate", closeDate.toString(),
                "source", "Website");
    }

    private long readId(MvcResult result) throws Exception {
        return readJson(result).path("id").asLong();
    }

    private com.fasterxml.jackson.databind.JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    /** LinkedHashMap, so a null value is allowed and key order is predictable. */
    private Map<String, Object> map(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    /** A unique, valid email address for this test run. */
    private static String unique(String prefix) {
        return prefix + SEQ.incrementAndGet() + "@example.com";
    }

    /** A unique phone number matching the 10-digit rule (first digit 6-9). */
    private static String uniquePhone() {
        return "9" + String.format("%09d", SEQ.incrementAndGet());
    }
}
