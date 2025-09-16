package chunk.generate;

import chunk.data.Chunk;

public interface ChunkStage {
    // TODO should we also pass in the previous chunk stage here?
    Chunk generate(int x, int y, int z, ChunkProvider provider);
}
