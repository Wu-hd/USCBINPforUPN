package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("asset_pipe_section")
public class AssetPipeSectionEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sectionCode;
    private Long networkId;
    private String sectionName;
    private String pipeMaterial;
    private BigDecimal diameterMm;
    private BigDecimal buryDepthM;
    private Integer pipeAgeYear;
    private Integer oldFlag;
    private Integer renovationStatus;
    private Long startNodeId;
    private Long endNodeId;
    private BigDecimal latestHealthScore;
    private Integer latestRiskLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getPipeMaterial() {
        return pipeMaterial;
    }

    public void setPipeMaterial(String pipeMaterial) {
        this.pipeMaterial = pipeMaterial;
    }

    public BigDecimal getDiameterMm() {
        return diameterMm;
    }

    public void setDiameterMm(BigDecimal diameterMm) {
        this.diameterMm = diameterMm;
    }

    public BigDecimal getBuryDepthM() {
        return buryDepthM;
    }

    public void setBuryDepthM(BigDecimal buryDepthM) {
        this.buryDepthM = buryDepthM;
    }

    public Integer getPipeAgeYear() {
        return pipeAgeYear;
    }

    public void setPipeAgeYear(Integer pipeAgeYear) {
        this.pipeAgeYear = pipeAgeYear;
    }

    public Integer getOldFlag() {
        return oldFlag;
    }

    public void setOldFlag(Integer oldFlag) {
        this.oldFlag = oldFlag;
    }

    public Integer getRenovationStatus() {
        return renovationStatus;
    }

    public void setRenovationStatus(Integer renovationStatus) {
        this.renovationStatus = renovationStatus;
    }

    public Long getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(Long startNodeId) {
        this.startNodeId = startNodeId;
    }

    public Long getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(Long endNodeId) {
        this.endNodeId = endNodeId;
    }

    public BigDecimal getLatestHealthScore() {
        return latestHealthScore;
    }

    public void setLatestHealthScore(BigDecimal latestHealthScore) {
        this.latestHealthScore = latestHealthScore;
    }

    public Integer getLatestRiskLevel() {
        return latestRiskLevel;
    }

    public void setLatestRiskLevel(Integer latestRiskLevel) {
        this.latestRiskLevel = latestRiskLevel;
    }
}
