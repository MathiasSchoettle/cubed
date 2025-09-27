package chunk.generate.test;

import block.meta.BlockInfo;
import chunk.data.Chunk;
import chunk.generate.ChunkContext;
import chunk.generate.ChunkGenerationStage;
import chunk.generate.ChunkPosition;
import chunk.generate.ReadonlyChunk;

import static chunk.data.Chunk.CHUNK_SIZE;

public class GrassStage implements ChunkGenerationStage {

    private final BlockInfo air;
    private final BlockInfo dirt;
    private final BlockInfo grass;

    private static final int DIRT_DEPTH = 2;

    public GrassStage(BlockInfo air, BlockInfo dirt, BlockInfo grass) {
        this.air = air;
        this.dirt = dirt;
        this.grass = grass;
    }

    @Override
    public void generate(Chunk chunk, ChunkPosition position, ChunkContext context) {

        ReadonlyChunk above = context.getChunk("terrain", position.offsetY(1));

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE + DIRT_DEPTH; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {

            boolean blockAboveIsAir = getBlockId(chunk, above, x, y + 1, z) == air.id();

            if (getBlockId(chunk, above, x, y, z) != air.id() && blockAboveIsAir) {
                chunk.trySet(x, y, z, grass.id());

                for (int i = 1; i < DIRT_DEPTH; ++i) {
                    chunk.trySet(x, y - i, z, dirt.id());
                }
            }
        }
    }

    private int getBlockId(Chunk chunk, ReadonlyChunk above, int x, int y, int z) {
        if (y < CHUNK_SIZE) {
            return chunk.get(x, y, z);
        } else {
            return above.get(x, y - CHUNK_SIZE, z);
        }
    }
}
