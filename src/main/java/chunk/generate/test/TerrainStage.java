package chunk.generate.test;

import chunk.data.Chunk;
import chunk.generate.ChunkContext;
import chunk.generate.ChunkGenerationStage;
import chunk.generate.ChunkPosition;
import math.noise.SimplexNoise;

import static chunk.data.Chunk.CHUNK_SIZE;

public class TerrainStage implements ChunkGenerationStage {

    private final long seed;
    private final short airId;
    private final short stoneId;

    private static final double PLATEAU_FREQUENCY = 1.0 / 52.0;

    public TerrainStage(long seed, short airId, short stoneId) {
        this.seed = seed;
        this.airId = airId;
        this.stoneId = stoneId;
    }

    @Override
    public void generate(Chunk chunk, ChunkPosition position, ChunkContext context) {

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {
            int gx = (position.x() * CHUNK_SIZE) + x;
            int gy = (position.y() * CHUNK_SIZE) + y;
            int gz = (position.z() * CHUNK_SIZE) + z;

            var noise = SimplexNoise.noise2_ImproveX(seed, gx * PLATEAU_FREQUENCY, gz * PLATEAU_FREQUENCY);
            noise *= noise;

            var floorNoise = 70 * noise;

            if (gy > floorNoise) {
                chunk.set(x, y, z, airId);
            } else {
                chunk.set(x, y, z, stoneId);
            }
        }
    }
}
