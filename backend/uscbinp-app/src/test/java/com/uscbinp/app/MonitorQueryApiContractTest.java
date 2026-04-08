package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class MonitorQueryApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void historyShouldSortByCollectTimeAscAndApplyLimit() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 1.00,
                      "collectTime": "2026-04-08T10:00:00",
                      "traceId": "h-1"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 1.20,
                      "collectTime": "2026-04-08T10:01:00",
                      "traceId": "h-2"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 1.40,
                      "collectTime": "2026-04-08T10:02:00",
                      "traceId": "h-3"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/monitor/history")
                .param("pointId", "1001")
                .param("startTime", "2026-04-08T10:00:30")
                .param("endTime", "2026-04-08T10:03:00")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.items.length()").value(2))
            .andExpect(jsonPath("$.data.items[0].metricValue").value(1.20))
            .andExpect(jsonPath("$.data.items[1].metricValue").value(1.40));
    }

    @Test
    void historyInvalidTimeRangeShouldReturnValidationCode() throws Exception {
        mockMvc.perform(get("/api/monitor/history")
                .param("pointId", "1001")
                .param("startTime", "2026-04-08T10:03:00")
                .param("endTime", "2026-04-08T10:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("REQ_4000"))
            .andExpect(jsonPath("$.message").exists());
    }
}
