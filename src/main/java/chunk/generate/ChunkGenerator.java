package chunk.generate;

import block.BlockProvider;
import chunk.data.Chunk;
import chunk.data.ChunkKey;
import math.noise.SimplexNoise;

import static chunk.data.Chunk.CHUNK_SIZE;

// TODO multithread this.
public class ChunkGenerator {

    private final long seed;
    private final BlockProvider blockProvider;

    private static final double FREQUENCY = 1.0 / 12.0;
    private static final double PLATEAU_FREQUENCY = 1.0 / 45.0;

    public ChunkGenerator(long seed, BlockProvider blockProvider) {
        this.seed = seed;
        this.blockProvider = blockProvider;
    }

    public Chunk generate(ChunkKey key) {
        var chunk = new Chunk();

        // TODO these values could be stored on creation
        var airId = blockProvider.getBlockId("base:air");
        var dirtId = blockProvider.getBlockId("base:dirt");
        var grassId = blockProvider.getBlockId("base:grass");
        var stoneId = blockProvider.getBlockId("base:stone");
        var cobblestoneId = blockProvider.getBlockId("base:cobblestone");
//        var sandId = blockProvider.getBlockId("base:sand");

        if (key.y() > 0) {
            return chunk;
        }

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {

            int gx = (key.x() * CHUNK_SIZE) + x;
            int gy = (key.y() * CHUNK_SIZE) + y;
            int gz = (key.z() * CHUNK_SIZE) + z;

            var floorNoise = 5 * (SimplexNoise.noise2_ImproveX(seed, gx * PLATEAU_FREQUENCY, gz * PLATEAU_FREQUENCY) + 1);

            if (gy > floorNoise) {
                chunk.set(x, y, z, airId);
            } else {
                chunk.set(x, y, z, grassId);
            }

            continue;

//            var noise = SimplexNoise.noise3_ImproveXY(seed, gx * FREQUENCY, gy * FREQUENCY, gz * FREQUENCY);
//            var noiseAbove = SimplexNoise.noise3_ImproveXY(seed, gx * FREQUENCY, (gy + 1) * FREQUENCY, gz * FREQUENCY);
//
//            if (noise > 0) {
//                if (gy < 10) {
//                    if (noise < 0.5) {
//                        chunk.set(x, y, z, stoneId);
//                    } else {
//                        chunk.set(x,y ,z, cobblestoneId);
//                    }
//                } else {
//                    if (noiseAbove > 0) {
//                        chunk.set(x, y, z, dirtId);
//                    } else {
//                        chunk.set(x, y, z, grassId);
//                    }
//                }
//            } else {
//                chunk.set(x, y, z, airId);
//            }
        }

        return chunk;
    }
}
