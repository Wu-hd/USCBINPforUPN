package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("iot_measure_point")
public class IotMeasurePointEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String pointCode;
    private String pointName;
    private Long deviceId;
    private Long sectionId;
    private Long nodeId;
    private String metricType;
    private String unitName;
    private Integer sampleCycleSec;
    private BigDecimal thresholdMin;
    private BigDecimal thresholdMax;
    private Integer pointStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getSampleCycleSec() {
        return sampleCycleSec;
    }

    public void setSampleCycleSec(Integer sampleCycleSec) {
        this.sampleCycleSec = sampleCycleSec;
    }

    public BigDecimal getThresholdMin() {
        return thresholdMin;
    }

    public void setThresholdMin(BigDecimal thresholdMin) {
        this.thresholdMin = thresholdMin;
    }

    public BigDecimal getThresholdMax() {
        return thresholdMax;
    }

    public void setThresholdMax(BigDecimal thresholdMax) {
        this.thresholdMax = thresholdMax;
    }

    public Integer getPointStatus() {
        return pointStatus;
    }

    public void setPointStatus(Integer pointStatus) {
        this.pointStatus = pointStatus;
    }
}
