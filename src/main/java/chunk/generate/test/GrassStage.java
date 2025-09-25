package chunk.generate.test;

import chunk.data.Chunk;
import chunk.generate.ChunkContext;
import chunk.generate.ChunkGenerationStage;
import chunk.generate.ChunkPosition;
import chunk.generate.ReadonlyChunk;

import static chunk.data.Chunk.CHUNK_SIZE;

public class GrassStage implements ChunkGenerationStage {

    private final short airId;
    private final short dirtId;
    private final short grassId;

    private static final int DIRT_DEPTH = 3;

    public GrassStage(short airId, short dirtId, short grassId) {
        this.airId = airId;
        this.dirtId = dirtId;
        this.grassId = grassId;
    }

    @Override
    public void generate(Chunk chunk, ChunkPosition position, ChunkContext context) {

        ReadonlyChunk above = context.getChunk("terrain", position.offsetY(1));

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE + DIRT_DEPTH; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {

            boolean blockAboveIsAir = getBlock(chunk, above, x, y + 1, z) == airId;

            if (getBlock(chunk, above, x, y, z) != airId && blockAboveIsAir) {
                chunk.trySet(x, y, z, grassId);

                for (int i = 1; i < DIRT_DEPTH; ++i) {
                    chunk.trySet(x, y - i, z, dirtId);
                }
            }
        }
    }

    private short getBlock(Chunk chunk, ReadonlyChunk above, int x, int y, int z) {
        if (y < CHUNK_SIZE) {
            return chunk.get(x, y, z);
        } else {
            return above.get(x, y - CHUNK_SIZE, z);
        }
    }
}
