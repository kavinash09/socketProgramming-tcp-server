package com.avinash.socket.transport.tcp.selector;

import java.nio.channels.Selector;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SelectorTaskQueue {
    private final Selector selector;
    private final Queue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    public SelectorTaskQueue(Selector selector) {
        this.selector = selector;
    }

    public void submit(Runnable task) {
        pendingTasks.add(Objects.requireNonNull(task, "task"));
        selector.wakeup();
    }
    public void runPendingTask() {
        Runnable task;
        while((task = pendingTasks.poll()) != null) {
            task.run();
        }
    }

}
