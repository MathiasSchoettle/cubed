package chunk.generate;

import chunk.data.Chunk;
import utils.data.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkGeneration {

    private final ChunkGenerator generator;

    private static final String INITIAL_STAGE = "_initial";

    public Chunk generate(ChunkPosition position) {
        return generator.generate(position);
    }

    private ChunkGeneration(List<Tuple<String, ChunkGenerationStage>> stages) {

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

    public static ChunkGenerationBuilder builder() {
        return new ChunkGenerationBuilder();
    }

    public static class ChunkGenerationBuilder {

        private final List<Tuple<String, ChunkGenerationStage>> stages = new ArrayList<>();

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

        public ChunkGeneration build() {
            return new ChunkGeneration(stages);
        }
    }
}
