package com.uscbinp.app;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class WorkOrderApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createFromAlertShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/workorders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sourceType":"ALERT_EVENT",
                      "sourceId":9001,
                      "title":"repair",
                      "targetType":"PIPE_SECTION",
                      "targetId":3001,
                      "regionCode":"3301",
                      "assigneeUserId":101
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.sourceType").value("ALERT_EVENT"));
    }

    @Test
    void startThenFinishShouldTransitToDone() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/workorders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sourceType":"ALERT_EVENT",
                      "sourceId":9001,
                      "title":"repair2",
                      "targetType":"PIPE_SECTION",
                      "targetId":3001,
                      "regionCode":"3301",
                      "assigneeUserId":101
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();
        Number workOrderIdValue = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
        Long workOrderId = workOrderIdValue.longValue();

        mockMvc.perform(put("/api/workorders/{id}/start", workOrderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.workStatus").value(2));

        mockMvc.perform(put("/api/workorders/{id}/finish", workOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"resultSummary":"done"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.workStatus").value(3));
    }
}
