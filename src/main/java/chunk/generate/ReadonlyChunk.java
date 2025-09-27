package chunk.generate;

import chunk.data.Chunk;

public class ReadonlyChunk {

    private final Chunk chunk;

    public ReadonlyChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public int get(int x, int y, int z) {
        return chunk.get(x, y, z);
    }
}
