package chunk.generate;

import chunk.data.Chunk;

public interface ChunkGenerationStage {
    void generate(Chunk chunk, ChunkPosition position, ChunkContext context);
}
