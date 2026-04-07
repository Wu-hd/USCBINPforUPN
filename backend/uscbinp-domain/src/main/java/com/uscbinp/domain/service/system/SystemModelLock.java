package com.uscbinp.domain.service.system;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Component
public class SystemModelLock {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public <T> T withReadLock(Supplier<T> action) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return action.get();
        } finally {
            readLock.unlock();
        }
    }

    public void withReadLock(Runnable action) {
        withReadLock(() -> {
            action.run();
            return null;
        });
    }

    public <T> T withWriteLock(Supplier<T> action) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return action.get();
        } finally {
            writeLock.unlock();
        }
    }

    public void withWriteLock(Runnable action) {
        withWriteLock(() -> {
            action.run();
            return null;
        });
    }
}
