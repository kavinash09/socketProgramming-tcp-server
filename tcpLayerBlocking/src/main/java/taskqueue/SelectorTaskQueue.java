package taskqueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SelectorTaskQueue {
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    public void submit(Runnable task) {
        tasks.add(task);
    }
    public void runPendingTask() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }
}
