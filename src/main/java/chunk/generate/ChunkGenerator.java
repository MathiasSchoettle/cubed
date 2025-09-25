package chunk.generate;

import chunk.data.Chunk;

public class ChunkGenerator {

    private final String stage;

    private final ChunkGenerationStage generationStage;

    private final ChunkContext context;

    private final ChunkGenerator previousGenerator;

    private final ChunkCache cache;

    public ChunkGenerator(String stage, ChunkGenerationStage generationStage, ChunkContext context, ChunkGenerator previousGenerator, ChunkCache cache) {
        this.stage = stage;
        this.generationStage = generationStage;
        this.context = context;
        this.previousGenerator = previousGenerator;
        this.cache = cache;
    }

    public Chunk generate(ChunkPosition position) {

        if (previousGenerator == null) {
            return new Chunk();
        }

        var chunkOptional = cache.get(stage, position);

        if (chunkOptional.isPresent()) {
            return chunkOptional.get();
        }

        var chunk = previousGenerator.generate(position);
        generationStage.generate(chunk, position, context);

        cache.store(Chunk.copy(chunk), stage, position);

        return chunk;
    }
}
