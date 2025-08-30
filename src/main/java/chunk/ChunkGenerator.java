package chunk;

import block.BlockProvider;
import chunk.data.Chunk;
import chunk.data.ChunkKey;
import math.noise.SimplexNoise;

import static chunk.data.Chunk.CHUNK_SIZE;

public class ChunkGenerator {

    private final long seed;
    private final BlockProvider blockProvider;

    private static final double FREQUENCY = 1.0 / 12.0;

    public ChunkGenerator(long seed, BlockProvider blockProvider) {
        this.seed = seed;
        this.blockProvider = blockProvider;
    }

    public Chunk generate(ChunkKey key) {
        var chunk = new Chunk();
        var airId = blockProvider.getBlockId("base:air");
        var dirtId = blockProvider.getBlockId("base:dirt");

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {
            if (SimplexNoise.noise3_ImproveXY(
                    seed,
                    (key.x() * CHUNK_SIZE + x) * FREQUENCY,
                    (key.y() * CHUNK_SIZE + y) * FREQUENCY,
                    (key.z() * CHUNK_SIZE + z) * FREQUENCY
            ) > 0) {
                chunk.set(x, y, z, dirtId);
            } else {
                chunk.set(x, y, z, airId);
            }
        }

        return chunk;
    }
}
