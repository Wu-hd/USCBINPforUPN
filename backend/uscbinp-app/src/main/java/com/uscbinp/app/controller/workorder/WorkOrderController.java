package com.uscbinp.app.controller.workorder;

import com.uscbinp.app.controller.workorder.dto.AssignWorkOrderRequest;
import com.uscbinp.app.controller.workorder.dto.CreateWorkOrderRequest;
import com.uscbinp.app.controller.workorder.dto.FinishWorkOrderRequest;
import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.domain.service.workorder.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ApiResponse<WorkOrderService.WorkOrderView> create(@Valid @RequestBody CreateWorkOrderRequest request) {
        return ApiResponse.ok(workOrderService.create(new WorkOrderService.CreateWorkOrderCommand(
            request.sourceType(),
            request.sourceId(),
            request.title(),
            request.targetType(),
            request.targetId(),
            request.regionCode(),
            request.assigneeUserId(),
            request.expectFinishTime()
        )));
    }

    @GetMapping
    public ApiResponse<WorkOrderService.PageResult> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize,
        @RequestParam(required = false) Integer workStatus,
        @RequestParam(required = false) String sourceType,
        @RequestParam(required = false) Long sourceId
    ) {
        return ApiResponse.ok(workOrderService.pageQuery(new WorkOrderService.PageQuery(
            pageNum,
            pageSize,
            workStatus,
            sourceType,
            sourceId
        )));
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<WorkOrderService.WorkOrderView> assign(
        @PathVariable Long id,
        @Valid @RequestBody AssignWorkOrderRequest request
    ) {
        return ApiResponse.ok(workOrderService.assign(new WorkOrderService.AssignWorkOrderCommand(
            id,
            request.assigneeUserId(),
            0L
        )));
    }

    @PutMapping("/{id}/start")
    public ApiResponse<WorkOrderService.WorkOrderView> start(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.start(new WorkOrderService.StartWorkOrderCommand(id, 0L)));
    }

    @PutMapping("/{id}/finish")
    public ApiResponse<WorkOrderService.WorkOrderView> finish(
        @PathVariable Long id,
        @RequestBody(required = false) FinishWorkOrderRequest request
    ) {
        String resultSummary = request == null ? null : request.resultSummary();
        return ApiResponse.ok(workOrderService.finish(new WorkOrderService.FinishWorkOrderCommand(id, 0L, resultSummary)));
    }
}
