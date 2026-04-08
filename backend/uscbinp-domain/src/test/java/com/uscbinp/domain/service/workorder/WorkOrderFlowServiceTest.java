package com.uscbinp.domain.service.workorder;

import com.uscbinp.domain.service.alert.AlertEventService;
import com.uscbinp.domain.service.alert.impl.AlertEventServiceImpl;
import com.uscbinp.domain.service.workorder.impl.InMemoryAlertWorkOrderStore;
import com.uscbinp.domain.service.workorder.impl.WorkOrderLogServiceImpl;
import com.uscbinp.domain.service.workorder.impl.WorkOrderServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkOrderFlowServiceTest {

    @Test
    void finishShouldWriteStatusLog() {
        InMemoryAlertWorkOrderStore store = new InMemoryAlertWorkOrderStore();
        WorkOrderLogService logService = new WorkOrderLogServiceImpl(store);
        WorkOrderService workOrderService = new WorkOrderServiceImpl(store, logService);
        AlertEventService alertEventService = new AlertEventServiceImpl(store);

        AlertEventService.AlertView alert = alertEventService.confirm(9001L, 101L);
        WorkOrderService.WorkOrderView created = workOrderService.create(new WorkOrderService.CreateWorkOrderCommand(
            "ALERT_EVENT",
            alert.id(),
            "pressure high",
            "PIPE_SECTION",
            3001L,
            "3301",
            101L,
            null
        ));
        workOrderService.start(new WorkOrderService.StartWorkOrderCommand(created.id(), 101L));
        workOrderService.finish(new WorkOrderService.FinishWorkOrderCommand(created.id(), 101L, "done"));

        assertEquals(3, store.listLogs(created.id()).size());
        assertEquals("FINISH", store.listLogs(created.id()).get(2).getActionType());
        assertEquals(3, workOrderService.pageQuery(new WorkOrderService.PageQuery(1, 10, null, null, null)).totalLogs());
    }
}
