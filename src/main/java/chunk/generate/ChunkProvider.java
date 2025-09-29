package chunk.generate;

import chunk.data.Chunk;
import chunk.generate.stage.ChunkGenerationStage;
import threading.TaskHandler;
import utils.data.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class ChunkProvider {

    private final ChunkGenerator generator;

    private static final String INITIAL_STAGE = "_initial";

    private final TaskHandler taskHandler;

    private ChunkProvider(List<Tuple<String, ChunkGenerationStage>> stages, TaskHandler taskHandler) {

        this.taskHandler = taskHandler;

        var cache = new ChunkCache();

        Map<String, ChunkGenerator> generators = new HashMap<>();

        ChunkGenerationStage previousStage = null;
        ChunkContext previousContext = null;
        ChunkGenerator previousGenerator = null;
        String name = INITIAL_STAGE;

        for (var tuple : stages) {
            var stage = tuple.second();

            var generator = new ChunkGenerator(name, previousStage, previousContext, previousGenerator, cache);
            generators.put(name, generator);

            var context = new ChunkContext(new HashMap<>(generators));

            previousGenerator = generator;
            previousStage = stage;
            previousContext = context;
            name = tuple.first();
        }

        this.generator = new ChunkGenerator(name, previousStage, previousContext, previousGenerator, cache);
    }

    public Future<Chunk> provide(ChunkPosition position) {
        return taskHandler.submitGenerationTask(() -> generator.generate(position));
    }

    public static ChunkGenerationBuilder builder(TaskHandler handler) {
        return new ChunkGenerationBuilder(handler);
    }

    public static class ChunkGenerationBuilder {

        private final TaskHandler taskHandler;

        private final List<Tuple<String, ChunkGenerationStage>> stages = new ArrayList<>();

        public ChunkGenerationBuilder(TaskHandler taskHandler) {
            this.taskHandler = taskHandler;
        }

        public ChunkGenerationBuilder addStage(String name, ChunkGenerationStage stage) {

            if (INITIAL_STAGE.equals(name) || name == null) {
                throw new IllegalArgumentException("invalid stage name: " + name);
            }

            if (stages.stream().anyMatch(entry -> entry.first().equals(name))) {
                throw new IllegalArgumentException("Stage with name already exists: " + name);
            }

            stages.add(new Tuple<>(name, stage));
            return this;
        }

        public ChunkProvider build() {
            return new ChunkProvider(stages, taskHandler);
        }
    }
}
