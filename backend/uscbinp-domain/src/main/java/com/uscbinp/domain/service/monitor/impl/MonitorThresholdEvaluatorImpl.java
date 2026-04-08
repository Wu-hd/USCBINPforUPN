package com.uscbinp.domain.service.monitor.impl;

import com.uscbinp.domain.service.monitor.MonitorThresholdEvaluator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MonitorThresholdEvaluatorImpl implements MonitorThresholdEvaluator {

    @Override
    public int evaluate(BigDecimal value, BigDecimal thresholdMin, BigDecimal thresholdMax) {
        if (value == null) {
            return 1;
        }
        if (thresholdMin != null && value.compareTo(thresholdMin) < 0) {
            return 1;
        }
        if (thresholdMax != null && value.compareTo(thresholdMax) > 0) {
            return 1;
        }
        return 0;
    }
}
