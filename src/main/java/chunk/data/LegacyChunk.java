package chunk.data;

import math.noise.PerlinNoise3D;
import utils.data.FloatArray;
import utils.data.ShortArray;

public class LegacyChunk {

    private static final int CHUNK_SIZE = 16;
    private static final int SLICE_SIZE = CHUNK_SIZE * CHUNK_SIZE;
    private static final int BLOCK_COUNT = SLICE_SIZE * CHUNK_SIZE;

    static final int[][] NORMALS = {
            {+1,  0,  0}, {-1,  0,  0},
            { 0, +1,  0}, { 0, -1,  0},
            { 0,  0, +1}, { 0,  0, -1}
    };
    static final int[][][] FACE_CORNERS = {
            {{1,0,0},{1,0,1},{1,1,1},{1,1,0}}, // +X
            {{0,0,0},{0,1,0},{0,1,1},{0,0,1}}, // -X
            {{0,1,0},{1,1,0},{1,1,1},{0,1,1}}, // +Y
            {{0,0,0},{0,0,1},{1,0,1},{1,0,0}}, // -Y
            {{0,0,1},{0,1,1},{1,1,1},{1,0,1}}, // +Z
            {{0,0,0},{1,0,0},{1,1,0},{0,1,0}}  // -Z
    };
    float[][][] UV_OFFSETS = {
            { {0,0}, {1,0}, {1,1}, {0,1} }, // +X
            { {1,0}, {0,0}, {0,1}, {1,1} }, // -X (flipped horizontally)
            { {0,1}, {1,1}, {1,0}, {0,0} }, // +Y (flipped vertically)
            { {0,0}, {1,0}, {1,1}, {0,1} }, // -Y
            { {0,0}, {1,0}, {1,1}, {0,1} }, // +Z
            { {0,0}, {1,0}, {1,1}, {0,1} }, // -Z
    };

    private final boolean[] blocks = new boolean[BLOCK_COUNT];
    public ShortArray indices = new ShortArray();
    public FloatArray vertices = new FloatArray();

    public LegacyChunk() {

        var noise = new PerlinNoise3D(2);

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {
            blocks[x + y * CHUNK_SIZE + z * SLICE_SIZE] = noise.octaveNoise(x * 0.1f, y * 0.1f, z * 0.1f, 3, 1) > 0.4;
        }

        indices.init(40_000);
        vertices.init(20_000);
    }

    public void remesh() {

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {
            if (!get(x, y, z)) {
                continue;
            }

            double rand = Math.random();
            float layer = rand < 0.33 ? 0 : rand < 0.66 ? 1 : 2;

            for (int direction = 0; direction < NORMALS.length; ++direction) {

                var directionVector = NORMALS[direction];

                var neighbourIsSolid = hasBlock(
                        x + directionVector[0],
                        y + directionVector[1],
                        z + directionVector[2]
                );

                if (neighbourIsSolid) {
                    continue;
                }

                int base = vertices.size() / 9;

                for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                    // positions
                    var corner = FACE_CORNERS[direction][cornerIndex];
                    vertices
                            .push(x + corner[0])
                            .push(z + corner[2])
                            .push(y + corner[1]);

                    // normals
                    var normals = NORMALS[direction];
                    vertices.push(normals[0]).push(normals[1]).push(normals[2]);

                    // texture coordinates
                    var uvs = UV_OFFSETS[direction][cornerIndex];
                    vertices.push(uvs[0]).push(uvs[1]);

                    // texture index
                    vertices.push(layer);
                }

                indices
                        .push(base).push(base + 1).push(base + 2)
                        .push(base).push(base + 2).push(base + 3);
            }
        }
    }

    private boolean get(int x, int y, int z) {
        return blocks[x + y * CHUNK_SIZE + z * SLICE_SIZE];
    }

    private boolean hasBlock(int x, int y, int z) {
        if (
                x < 0 || x >= CHUNK_SIZE ||
                y < 0 || y >= CHUNK_SIZE ||
                z < 0 || z >= CHUNK_SIZE
        ) {
            return false;
        }

        return get(x, y, z);
    }
}
