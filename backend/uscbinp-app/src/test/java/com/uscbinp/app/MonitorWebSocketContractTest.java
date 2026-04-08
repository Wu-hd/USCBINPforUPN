package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class MonitorWebSocketContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Test
    void ingestShouldPublishMonitorStompMessages() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 2.20,
                      "qualityFlag": 1,
                      "traceId": "ws-1"
                    }
                    """))
            .andExpect(status().isOk());

        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/monitor/stream"), any(Object.class));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/monitor/points/1001"), any(Object.class));
    }
}
