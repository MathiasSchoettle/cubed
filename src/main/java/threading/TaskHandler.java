package threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskHandler {

    private final ExecutorService meshingExecutor;

    private final ExecutorService generationExecutor;

    public TaskHandler(ExecutorService meshingExecutor, ExecutorService generationExecutor) {
        this.meshingExecutor = meshingExecutor;
        this.generationExecutor = generationExecutor;
    }

    public <T> Future<T> submitMeshingTask(Callable<T> task) {
        return this.meshingExecutor.submit(task);
    }

    public <T> Future<T> submitGenerationTask(Callable<T> task) {
        return this.generationExecutor.submit(task);
    }

    public void shutdown() {
        this.meshingExecutor.shutdown();
        this.generationExecutor.shutdown();
    }

    // 2² = 4
    // 2³ = 8

    // 2^12 = 4096 -> 12 bits
}
