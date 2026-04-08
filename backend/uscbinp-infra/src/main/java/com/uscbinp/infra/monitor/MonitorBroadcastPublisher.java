package com.uscbinp.infra.monitor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class MonitorBroadcastPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public MonitorBroadcastPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(MonitorRealtimeMessage message) {
        messagingTemplate.convertAndSend("/topic/monitor/stream", message);
        messagingTemplate.convertAndSend("/topic/monitor/points/" + message.pointId(), message);
    }

    public record MonitorRealtimeMessage(
        Long pointId,
        String metricType,
        BigDecimal currentValue,
        LocalDateTime collectTime,
        Integer qualityFlag,
        Integer alarmFlag,
        String traceId,
        String alertCode
    ) {
    }
}
