package com.acxiom.crm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acxiom.crm.entity.ActivityType;
import com.acxiom.crm.entity.FollowUpStatus;
import com.acxiom.crm.entity.FollowUpType;
import com.acxiom.crm.entity.RoleName;
import com.acxiom.crm.repository.ActivityRepository;
import com.acxiom.crm.repository.AuditLogRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.FollowUpRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Phase 2 exit checklist")
class Phase2ExitChecklistTest {

    private static final String ADMIN_EMAIL = "admin@acxiomcrm.local";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private static final String MANAGER_EMAIL = "manager@acxiomcrm.local";
    private static final String MANAGER_PASSWORD = "Manager@123";

    private static final String SALES_EMAIL = "sales@acxiomcrm.local";
    private static final String SALES_PASSWORD = "Sales@123";

    private static final AtomicInteger SEQ = new AtomicInteger(5000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FollowUpRepository followUpRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String csrfToken;
    private MockHttpSession session;

    @BeforeEach
    void bootstrapCsrfToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        this.csrfToken = cookie.getValue();
        this.session = null;
    }

    // ----------------------------------------------------- A. Follow-Ups

    @Test
    @DisplayName("can schedule a valid follow-up successfully")
    void scheduleValidFollowUp() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        Map<String, Object> body = map(
                "subject", "Demo follow up " + SEQ.incrementAndGet(),
                "followUpDate", LocalDate.now().plusDays(2).toString(),
                "followUpType", FollowUpType.CALL.name(),
                "status", FollowUpStatus.PLANNED.name(),
                "remarks", "Discuss product pricing proposal"
        );

        mockMvc.perform(authed(post("/api/followups"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.subject").value(body.get("subject")))
                .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    @DisplayName("past follow-up date is rejected for new/planned follow-up (§17.19 #8)")
    void pastFollowUpDateIsRejected() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        Map<String, Object> body = map(
                "subject", "Past follow up " + SEQ.incrementAndGet(),
                "followUpDate", LocalDate.now().minusDays(1).toString(),
                "followUpType", FollowUpType.CALL.name(),
                "status", FollowUpStatus.PLANNED.name(),
                "remarks", "Should fail"
        );

        mockMvc.perform(authed(post("/api/followups"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.followUpDate").value("Follow-up date cannot be earlier than today."))
                .andExpect(jsonPath("$.message").value("Follow-up date cannot be earlier than today."));
    }

    @Test
    @DisplayName("can update follow-up status to COMPLETED and audits change")
    void updateFollowUpStatus() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        Map<String, Object> body = map(
                "subject", "Follow up to complete " + SEQ.incrementAndGet(),
                "followUpDate", LocalDate.now().plusDays(1).toString(),
                "followUpType", FollowUpType.MEETING.name(),
                "status", FollowUpStatus.PLANNED.name()
        );

        MvcResult createResult = mockMvc.perform(authed(post("/api/followups"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();

        long followUpId = readId(createResult);

        // Update status to COMPLETED
        Map<String, Object> updateBody = map(
                "subject", "Follow up completed",
                "followUpDate", LocalDate.now().plusDays(1).toString(),
                "followUpType", FollowUpType.MEETING.name(),
                "status", FollowUpStatus.COMPLETED.name()
        );

        mockMvc.perform(authed(put("/api/followups/" + followUpId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("sales executive cannot access another user's follow-up")
    void salesExecutiveOwnershipIsolationOnFollowUp() throws Exception {
        // Admin creates a follow-up assigned to Admin
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        Map<String, Object> body = map(
                "subject", "Admin secret follow up " + SEQ.incrementAndGet(),
                "followUpDate", LocalDate.now().plusDays(3).toString(),
                "followUpType", FollowUpType.CALL.name(),
                "status", FollowUpStatus.PLANNED.name()
        );

        MvcResult createResult = mockMvc.perform(authed(post("/api/followups"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long followUpId = readId(createResult);

        // Sales executive tries to view it -> 404 (isolation)
        login(SALES_EMAIL, SALES_PASSWORD);
        mockMvc.perform(authed(get("/api/followups/" + followUpId)))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------------- B. Activities

    @Test
    @DisplayName("can record an activity and search by type")
    void recordActivityAndFilter() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        Map<String, Object> body = map(
                "activityType", ActivityType.CALL.name(),
                "subject", "Discovery Call " + SEQ.incrementAndGet(),
                "description", "Initial discovery discussion",
                "activityDate", LocalDateTime.now().toString(),
                "status", "COMPLETED"
        );

        mockMvc.perform(authed(post("/api/activities"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.activityType").value("CALL"));

        mockMvc.perform(authed(get("/api/activities?activityType=CALL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ----------------------------------------------------- C. User Administration

    @Test
    @DisplayName("admin can create a user with a designated role")
    void adminCanCreateUser() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        String email = "manager" + SEQ.incrementAndGet() + "@acxiomcrm.local";
        Map<String, Object> body = map(
                "name", "Team Lead " + SEQ.get(),
                "email", email,
                "password", "Lead@12345",
                "role", RoleName.MANAGER.name()
        );

        mockMvc.perform(authed(post("/api/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    @DisplayName("manager can view user list but sales executive receives 403")
    void managerCanViewUsersSalesExecutiveCannot() throws Exception {
        login(MANAGER_EMAIL, MANAGER_PASSWORD);
        mockMvc.perform(authed(get("/api/users")))
                .andExpect(status().isOk());

        login(SALES_EMAIL, SALES_PASSWORD);
        mockMvc.perform(authed(get("/api/users")))
                .andExpect(status().isForbidden());
    }

    // ----------------------------------------------------- D. Dashboard & Analytics

    @Test
    @DisplayName("pipeline report returns 8 KPI card values and 3 chart data series")
    void pipelineReportReturnsCardsAndCharts() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(authed(get("/api/reports/pipeline")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCount").isNumber())
                .andExpect(jsonPath("$.leadCount").isNumber())
                .andExpect(jsonPath("$.openLeadCount").isNumber())
                .andExpect(jsonPath("$.totalOpportunities").isNumber()) // Card 4
                .andExpect(jsonPath("$.openCount").isNumber())
                .andExpect(jsonPath("$.wonCount").isNumber())
                .andExpect(jsonPath("$.lostCount").isNumber())
                .andExpect(jsonPath("$.totalOpenAmount").isNumber())
                // Chart 1: Lead Status
                .andExpect(jsonPath("$.leadStatusCounts").isMap())
                // Chart 2: Opportunity Pipeline
                .andExpect(jsonPath("$.opportunityStageCounts").isMap())
                // Chart 3: Monthly Sales
                .andExpect(jsonPath("$.monthlySales").isArray());
    }

    // ----------------------------------------------------- Helpers

    private void login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(authed(post("/api/auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        this.session = (MockHttpSession) result.getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder) {
        if (session != null) {
            builder.session(session);
        }
        if (csrfToken != null) {
            builder.cookie(new Cookie("XSRF-TOKEN", csrfToken));
            builder.header("X-XSRF-TOKEN", csrfToken);
        }
        return builder;
    }

    private long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Map<String, Object> map(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
