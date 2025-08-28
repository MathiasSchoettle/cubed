package chunk;

import utils.data.FloatArray;
import utils.data.ShortArray;

import java.util.Random;

public class Chunk {

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

    public Chunk() {
        var rand = new Random();
        for (int i = 0; i < blocks.length; ++i) {
            blocks[i] = rand.nextBoolean();
        }
        indices.init(40_000);
        vertices.init(20_000);
    }

    public void remesh() {

        for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {
            if (!get(x, y, z)) {
                continue;
            }

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

                int base = vertices.size() / 8;
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
