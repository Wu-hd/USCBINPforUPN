package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ops_work_order")
public class OpsWorkOrderEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String workOrderCode;
    private String sourceType;
    private Long sourceId;
    private String title;
    private String targetType;
    private Long targetId;
    private String regionCode;
    private Long assigneeUserId;
    private Integer workStatus;
    private LocalDateTime expectFinishTime;
    private LocalDateTime actualFinishTime;
    private String resultSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public Integer getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(Integer workStatus) {
        this.workStatus = workStatus;
    }

    public LocalDateTime getExpectFinishTime() {
        return expectFinishTime;
    }

    public void setExpectFinishTime(LocalDateTime expectFinishTime) {
        this.expectFinishTime = expectFinishTime;
    }

    public LocalDateTime getActualFinishTime() {
        return actualFinishTime;
    }

    public void setActualFinishTime(LocalDateTime actualFinishTime) {
        this.actualFinishTime = actualFinishTime;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }
}
