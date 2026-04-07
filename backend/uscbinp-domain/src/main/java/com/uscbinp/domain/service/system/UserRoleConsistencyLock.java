package com.uscbinp.domain.service.system;

import org.springframework.stereotype.Component;

@Component
public class UserRoleConsistencyLock {

    private final Object monitor = new Object();

    public Object monitor() {
        return monitor;
    }
}
