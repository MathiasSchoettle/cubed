package chunk.generate.test;

import block.meta.BlockInfo;
import chunk.data.Chunk;
import chunk.generate.ChunkContext;
import chunk.generate.ChunkGenerationStage;
import chunk.generate.ChunkPosition;
import math.noise.SimplexNoise;

import static chunk.data.Chunk.CHUNK_SIZE;

public class TerrainStage implements ChunkGenerationStage {

    private final long seed;
    private final BlockInfo air;
    private final BlockInfo stone;

    private static final double PLATEAU_FREQUENCY = 1.0 / 52.0;

    public TerrainStage(long seed, BlockInfo air, BlockInfo stone) {
        this.seed = seed;
        this.air = air;
        this.stone = stone;
    }

    @Override
    public void generate(Chunk chunk, ChunkPosition position, ChunkContext context) {
        for (int x = 0; x < CHUNK_SIZE; ++x) {
            for (int y = 0; y < CHUNK_SIZE; ++y) {
                for (int z = 0; z < CHUNK_SIZE; ++z) {
                    int gx = (position.x() * CHUNK_SIZE) + x;
                    int gy = (position.y() * CHUNK_SIZE) + y;
                    int gz = (position.z() * CHUNK_SIZE) + z;

                    // Normalized coords
                    double nx = gx * 0.002;
                    double ny = gy * 0.002;
                    double nz = gz * 0.002;

                    // --- Main ridged mountains ---
                    double ridge = 1.0 - Math.abs(SimplexNoise.noise3_ImproveXZ(seed, nx * 1.2, ny * 1.2, nz * 1.2));
                    ridge = Math.pow(ridge, 3.0) * 1.8; // sharp but believable peaks

                    // --- Canyon layer (broad valleys) ---
                    double canyon = Math.abs(SimplexNoise.noise2_ImproveX(seed + 2000, nx * 0.6, nz * 0.6)) * 2;
                    canyon = (0.45 - canyon) * 2.5; // deep but not extreme

                    // --- Small detail noise (rockiness) ---
                    double detail = SimplexNoise.noise3_ImproveXZ(seed + 4000, nx * 10.0, ny * 15.0, nz * 15.00) * 0.5;

                    // --- Vertical shaping ---
                    double verticalFalloff = gy * -0.03; // controls overall mountain height

                    // --- Combine into density ---
                    double density = verticalFalloff + ridge + canyon + detail;

                    if (density > 0) {
                        chunk.set(x, y, z, stone.id());
                    } else {
                        chunk.set(x, y, z, air.id());
                    }
                }
            }
        }
    }


}
