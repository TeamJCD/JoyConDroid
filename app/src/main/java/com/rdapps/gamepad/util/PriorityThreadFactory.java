package com.rdapps.gamepad.util;

import android.os.Process;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory that applies a priority to the threads it creates.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private final int threadPriority;
    private final String prefix;
    private final boolean addThreadNumber;

    private final boolean daemon;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public PriorityThreadFactory(
            int threadPriority, boolean daemon, String prefix, boolean addThreadNumber) {
        this.threadPriority = threadPriority;
        this.daemon = daemon;
        this.prefix = prefix;
        this.addThreadNumber = addThreadNumber;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = () -> {
            try {
                Process.setThreadPriority(threadPriority);
            } catch (Throwable t) {
                // just to be safe
            }
            runnable.run();
        };
        final String name;
        if (addThreadNumber) {
            name = prefix + "-" + threadNumber.getAndIncrement();
        } else {
            name = prefix;
        }
        Thread thread = new Thread(wrapperRunnable, name);
        thread.setDaemon(daemon);
        return thread;
    }

}