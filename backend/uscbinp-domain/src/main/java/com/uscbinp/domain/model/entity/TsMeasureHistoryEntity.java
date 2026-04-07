package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("ts_measure_history")
public class TsMeasureHistoryEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pointId;
    private Long sectionId;
    private String metricType;
    private BigDecimal metricValue;
    private Integer qualityFlag;
    private LocalDateTime collectTime;
    private LocalDateTime receiveTime;
    private String calcTag;
    private String traceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public Integer getQualityFlag() {
        return qualityFlag;
    }

    public void setQualityFlag(Integer qualityFlag) {
        this.qualityFlag = qualityFlag;
    }

    public LocalDateTime getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(LocalDateTime collectTime) {
        this.collectTime = collectTime;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getCalcTag() {
        return calcTag;
    }

    public void setCalcTag(String calcTag) {
        this.calcTag = calcTag;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
