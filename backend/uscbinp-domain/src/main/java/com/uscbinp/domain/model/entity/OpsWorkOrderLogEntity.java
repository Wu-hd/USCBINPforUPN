package com.uscbinp.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ops_work_order_log")
public class OpsWorkOrderLogEntity extends BaseAuditEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long workOrderId;
    private String actionType;
    private Long operatorUserId;
    private LocalDateTime actionTime;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String actionDesc;
    private Long attachmentGroupId;
    private String locationText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public Integer getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Integer beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Integer getAfterStatus() {
        return afterStatus;
    }

    public void setAfterStatus(Integer afterStatus) {
        this.afterStatus = afterStatus;
    }

    public String getActionDesc() {
        return actionDesc;
    }

    public void setActionDesc(String actionDesc) {
        this.actionDesc = actionDesc;
    }

    public Long getAttachmentGroupId() {
        return attachmentGroupId;
    }

    public void setAttachmentGroupId(Long attachmentGroupId) {
        this.attachmentGroupId = attachmentGroupId;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }
}
