package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("asset_facility")
public class AssetFacilityEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String facilityCode;
    private Long sectionId;
    private Long nodeId;
    private String facilityType;
    private String facilityName;
    private String manufacturer;
    private LocalDate installDate;
    private Integer maintainCycleDay;
    private Integer facilityStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacilityCode() {
        return facilityCode;
    }

    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
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

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getInstallDate() {
        return installDate;
    }

    public void setInstallDate(LocalDate installDate) {
        this.installDate = installDate;
    }

    public Integer getMaintainCycleDay() {
        return maintainCycleDay;
    }

    public void setMaintainCycleDay(Integer maintainCycleDay) {
        this.maintainCycleDay = maintainCycleDay;
    }

    public Integer getFacilityStatus() {
        return facilityStatus;
    }

    public void setFacilityStatus(Integer facilityStatus) {
        this.facilityStatus = facilityStatus;
    }
}
