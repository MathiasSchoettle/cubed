package chunk.generate.stage;

import chunk.data.Chunk;
import chunk.generate.ChunkContext;
import chunk.generate.ChunkPosition;

public interface ChunkGenerationStage {
    void generate(Chunk chunk, ChunkPosition position, ChunkContext context);
}
