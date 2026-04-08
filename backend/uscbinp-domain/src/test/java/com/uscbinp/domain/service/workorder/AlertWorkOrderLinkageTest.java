package com.uscbinp.domain.service.workorder;

import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.alert.AlertEventService;
import com.uscbinp.domain.service.alert.impl.AlertEventServiceImpl;
import com.uscbinp.domain.service.workorder.impl.InMemoryAlertWorkOrderStore;
import com.uscbinp.domain.service.workorder.impl.WorkOrderLogServiceImpl;
import com.uscbinp.domain.service.workorder.impl.WorkOrderServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertWorkOrderLinkageTest {

    @Test
    void shouldCloseAlertAfterWorkOrderFinished() {
        InMemoryAlertWorkOrderStore store = new InMemoryAlertWorkOrderStore();
        WorkOrderLogService logService = new WorkOrderLogServiceImpl(store);
        WorkOrderService workOrderService = new WorkOrderServiceImpl(store, logService);
        AlertEventService alertEventService = new AlertEventServiceImpl(store);

        alertEventService.confirm(9001L, 101L);
        WorkOrderService.WorkOrderView order = workOrderService.create(new WorkOrderService.CreateWorkOrderCommand(
            "ALERT_EVENT",
            9001L,
            "repair",
            "PIPE_SECTION",
            3001L,
            "3301",
            101L,
            null
        ));

        assertThrows(BusinessException.class, () -> alertEventService.close(9001L, 101L));

        workOrderService.start(new WorkOrderService.StartWorkOrderCommand(order.id(), 101L));
        workOrderService.finish(new WorkOrderService.FinishWorkOrderCommand(order.id(), 101L, "done"));
        AlertEventService.AlertView closed = alertEventService.close(9001L, 101L);

        assertEquals(3, closed.alertStatus());
        assertEquals(order.id(), closed.workOrderId());
    }
}
