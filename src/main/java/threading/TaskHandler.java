package threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskHandler {

    private final ExecutorService meshingExecutor;

    public TaskHandler(ExecutorService meshingExecutor) {
        this.meshingExecutor = meshingExecutor;
    }

    public <T> Future<T> submitMeshingTask(Callable<T> task) {
        return this.meshingExecutor.submit(task);
    }

    public void shutdown() {
        this.meshingExecutor.shutdown();
    }
}
