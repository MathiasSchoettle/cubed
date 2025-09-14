package chunk;

import block.BlockProvider;
import chunk.data.ChunkData;
import chunk.data.ChunkKey;
import utils.data.FloatArray;
import utils.data.ShortArray;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static chunk.data.Chunk.CHUNK_SIZE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesher {

    private final BlockProvider blockProvider;

    private final Map<ChunkKey, ChunkGpuData> chunkReferences = new HashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Map<ChunkKey, Future<Tuple<ShortArray, FloatArray>>> futures = new HashMap<>();

    private final short airId;

    public ChunkMesher(BlockProvider blockProvider) {
        this.blockProvider = blockProvider;
        this.airId = blockProvider.getBlockId("base:air");
    }

    record Tuple<A, B>(A first, B second) {}

    public void mesh(ChunkKey key, ChunkData chunkData) {
        var future = executorService.submit(() -> {
            var indices = new ShortArray();
            var vertices = new FloatArray();

            for (int x = 0; x < CHUNK_SIZE; ++x) for (int y = 0; y < CHUNK_SIZE; ++y) for (int z = 0; z < CHUNK_SIZE; ++z) {

                var blockId = chunkData.chunk.get(x, y, z);

                if (blockId == airId) {
                    continue;
                }

                for (int direction = 0; direction < NORMALS.length; ++direction) {
                    var directionVector = NORMALS[direction];

                    var neighbourIsSolid = getBlock(
                            x,
                            y,
                            z,
                            directionVector[0],
                            directionVector[1],
                            directionVector[2],
                            chunkData,
                            airId
                    ) != airId;

                    if (neighbourIsSolid) {
                        continue;
                    }

                    int base = vertices.size() / 9;

                    for (int cornerIndex = 0; cornerIndex < 4; ++cornerIndex) {
                        // positions
                        var corner = FACE_CORNERS[direction][cornerIndex];
                        vertices
                                .push(x + corner[0])
                                .push(y + corner[1])
                                .push(z + corner[2]);

                        // normals
                        var normals = NORMALS[direction];
                        vertices.push(normals[0]).push(normals[1]).push(normals[2]);

                        // texture coordinates
                        var uvs = UV_OFFSETS[direction][cornerIndex];
                        vertices.push(uvs[0]).push(uvs[1]);

                        // texture layer
                        var textureIndex = blockProvider.getTextureIndex(blockId, direction);
                        vertices.push(textureIndex);
                    }

                    indices
                            .push(base).push(base + 2).push(base + 1)
                            .push(base).push(base + 3).push(base + 2);
                }
            }

            return new Tuple<>(indices, vertices);
        });
        futures.put(key, future);
    }

    public void tick() {
        List<ChunkKey> completedKeys = new ArrayList<>();

        for (var entry : futures.entrySet()) {
            var future = entry.getValue();
            var key = entry.getKey();

            if (future.isDone()) {
                completedKeys.add(key);

                try {
                    var result = future.get();

                    remove(key); // TODO: have a look at glBufferSubData, if we can reuse same objects even if amount of vertices change?

                    var indices = result.first;
                    var vertices = result.second;

                    if (vertices.size() == 0 && indices.size() == 0) {
                        // don't generate mesh for chunk which has no vertex data (it probably is just air)
                        continue;
                    }

                    var vao = glGenVertexArrays();
                    glBindVertexArray(vao);

                    var vbo = glGenBuffers();
                    glBindBuffer(GL_ARRAY_BUFFER, vbo);
                    glBufferData(GL_ARRAY_BUFFER, vertices.data, GL_STATIC_DRAW);

                    var ibo = glGenBuffers();
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.data, GL_STATIC_DRAW);

                    glEnableVertexAttribArray(0);
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0L);
                    glEnableVertexAttribArray(1);
                    glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3L * Float.BYTES);
                    glEnableVertexAttribArray(2);
                    glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6L * Float.BYTES);
                    glEnableVertexAttribArray(3);
                    glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8L * Float.BYTES);

                    glBindVertexArray(0);
                    glBindBuffer(GL_ARRAY_BUFFER, 0);

                    // store new reference
                    chunkReferences.put(key, new ChunkGpuData(vao, vbo, ibo, indices.size()));

                } catch (ExecutionException | InterruptedException e) {
                    System.out.println("Something went wrong while building mesh for chunk " + key + ": " + e.getMessage());
                }
            }
        }

        completedKeys.forEach(futures::remove);
    }

    public void remove(ChunkKey key) {
        var existing = chunkReferences.remove(key);
        if (existing != null) {
            glDeleteVertexArrays(existing.vao);
            glDeleteBuffers(existing.vbo);
            glDeleteBuffers(existing.ibo);
        }
    }

    public Optional<ChunkGpuData> getData(ChunkKey key) {
        var data = chunkReferences.get(key);
        return Optional.ofNullable(data);
    }

    private short getBlock(int x, int y, int z, int dx, int dy, int dz, ChunkData chunkData, short defaultId) {
        int nx = x + dx;
        int ny = y + dy;
        int nz = z + dz;

        if (
                nx >= 0 && nx < CHUNK_SIZE &&
                ny >= 0 && ny < CHUNK_SIZE &&
                nz >= 0 && nz < CHUNK_SIZE
        ) {
            return chunkData.chunk.get(nx, ny, nz);
        }

        int neighbourIndex;
        if (nx >= CHUNK_SIZE) neighbourIndex = 0; // +X
        else if (nx < 0) neighbourIndex = 1;       // -X
        else if (ny >= CHUNK_SIZE) neighbourIndex = 2; // +Y
        else if (ny < 0) neighbourIndex = 3;  // -Y
        else if (nz >= CHUNK_SIZE) neighbourIndex = 4; // +Z
        else neighbourIndex = 5;  // -Z

        var neighbour = chunkData.neighbours[neighbourIndex];

        if (neighbour == null) {
            return defaultId;
        }

        int cx = (nx + CHUNK_SIZE) % CHUNK_SIZE;
        int cy = (ny + CHUNK_SIZE) % CHUNK_SIZE;
        int cz = (nz + CHUNK_SIZE) % CHUNK_SIZE;

        return neighbour.chunk.get(cx, cy, cz);
    }

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
    // NOTE: I just permutated these values until the textures looked correct
    // it is very likely that there is a bug somewhere else and the permutation just masks it. Does it really matter?
    // maybe...
    float[][][] UV_OFFSETS = {
            { {1,1}, {0,1}, {0,0}, {1,0} }, // +X
            { {1,1}, {1,0}, {0,0}, {0,1} }, // -X

            { {0,1}, {1,1}, {1,0}, {0,0} }, // +Y
            { {0,0}, {1,0}, {1,1}, {0,1} }, // -Y

            { {0,1}, {0,0}, {1,0}, {1,1} }, // +Z
            { {1,1}, {0,1}, {0,0}, {1,0} }, // -Z
    };

    // TODO think about this
    public record ChunkGpuData(int vao, int vbo, int ibo, int indexCount){}
}
