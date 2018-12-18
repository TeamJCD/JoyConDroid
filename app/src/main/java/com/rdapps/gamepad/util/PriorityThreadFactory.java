package com.rdapps.gamepad.util;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory that applies a priority to the threads it creates.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;
    private final String mPrefix;
    private final boolean mAddThreadNumber;

    private final boolean daemon;

    private final AtomicInteger mThreadNumber = new AtomicInteger(1);

    public PriorityThreadFactory(int threadPriority, boolean daemon, String prefix, boolean addThreadNumber) {
        this.mThreadPriority = threadPriority;
        this.daemon = daemon;
        this.mPrefix = prefix;
        this.mAddThreadNumber = addThreadNumber;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = () -> {
            try {
                Process.setThreadPriority(mThreadPriority);
            } catch (Throwable t) {
                // just to be safe
            }
            runnable.run();
        };
        final String name;
        if (mAddThreadNumber) {
            name = mPrefix + "-" + mThreadNumber.getAndIncrement();
        } else {
            name = mPrefix;
        }
        Thread thread = new Thread(wrapperRunnable, name);
        thread.setDaemon(daemon);
        return thread;
    }

}