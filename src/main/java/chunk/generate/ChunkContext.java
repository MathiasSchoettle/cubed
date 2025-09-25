package chunk.generate;

import java.util.Map;

public class ChunkContext {

    private final Map<String, ChunkGenerator> generators;

    public ChunkContext(Map<String, ChunkGenerator> generators) {
        this.generators = generators;
    }

    public ReadonlyChunk getChunk(String stage, ChunkPosition position) {
        if (!generators.containsKey(stage)) {
            throw new IllegalArgumentException("Tried to use unrecognized generation stage: " + stage);
        }

        var chunk = generators.get(stage).generate(position);

        return new ReadonlyChunk(chunk);
    }
}
