package com.uscbinp.domain.service.monitor;

import java.math.BigDecimal;

public interface MonitorThresholdEvaluator {

    int evaluate(BigDecimal value, BigDecimal thresholdMin, BigDecimal thresholdMax);
}
