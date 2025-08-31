package chunk;

import chunk.data.ChunkData;
import chunk.data.ChunkKey;
import math.mat.Mat4;
import shader.ShaderManager;
import shader.uniform.Uniforms;

import java.util.HashMap;
import java.util.Map;

import static chunk.data.Chunk.CHUNK_SIZE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class ChunkManager {

    private final ChunkStorage storage;

    private final ChunkGenerator generator;

    private final ChunkMesher mesher;

    // TODO these following values are all very temporary
    //  we need a solution how to get uniforms and shader calls into something like a chunkRenderer, and how to
    //  and how to bind the current chunks model matrix to the uniforms object, as uniforms kind of want references
    private final Mat4 modelMatrix = Mat4.of(0);
    private final ShaderManager shaderManager;
    private final Uniforms uniforms;

    private final Map<ChunkKey, ChunkData> chunkMap = new HashMap<>();

    // TODO shader manager and uniforms are temporary
    public ChunkManager(ChunkStorage storage, ChunkGenerator generator, ChunkMesher mesher, ShaderManager shaderManager, Uniforms uniforms) {
        this.storage = storage;
        this.generator = generator;
        this.mesher = mesher;
        this.shaderManager = shaderManager;
        this.uniforms = uniforms;
        this.uniforms.mat4("model", modelMatrix);
    }

    public void load(ChunkKey key) {
        if (chunkMap.containsKey(key)) {
            return;
        }
        var chunk = storage.load(key).orElseGet(() -> generator.generate(key));
        var data = new ChunkData(chunk, getModelMatrix(key));
        setNeighbours(key, data);
        chunkMap.put(key, data);
    }

    // TODO do this in a single loop
    private void setNeighbours(ChunkKey key, ChunkData data) {
        for (int x = 0; x <= 1; x++) {
            int offset = 1 - (x * 2);
            var neighbour = chunkMap.get(new ChunkKey(key.x() + offset, key.y(), key.z()));

            if (neighbour != null) {
                data.neighbours[x] = neighbour;
                neighbour.neighbours[(x * -1) + 1] = data;
                neighbour.needsRemesh = true;
            }
        }

        for (int y = 0; y <= 1; y++) {
            int offset = 1 - (y * 2);
            var neighbour = chunkMap.get(new ChunkKey(key.x(), key.y() + offset, key.z()));

            if (neighbour != null) {
                data.neighbours[2 + y] = neighbour;
                neighbour.neighbours[2 + (y * -1) + 1] = data;
                neighbour.needsRemesh = true;
            }
        }

        for (int z = 0; z <= 1; z++) {
            int offset = 1 - (z * 2);
            var neighbour = chunkMap.get(new ChunkKey(key.x(), key.y(), key.z() + offset));

            if (neighbour != null) {
                data.neighbours[4 + z] = neighbour;
                neighbour.neighbours[4 + (z * -1) + 1] = data;
                neighbour.needsRemesh = true;
            }
        }
    }

    public void unload(ChunkKey key) {
        var data = chunkMap.remove(key);
        if (data != null) {

            // remove from neighbours
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 2; ++j) {
                    var neighbour = data.neighbours[2 * i + (j * -1) + 1];
                    if (neighbour != null) {
                        neighbour.neighbours[2 * i + j] = null;
                        neighbour.needsRemesh = true;
                    }
                }
            }

            mesher.remove(key);
            storage.persist(key, data.chunk);
        }
    }

    public void update() {
        for (var entry : chunkMap.entrySet()) {
            if (entry.getValue().needsRemesh) {
                var key = entry.getKey();
                var data = entry.getValue();

                mesher.mesh(key, data);
                data.needsRemesh = false;
            }
        }
    }

    private Mat4 getModelMatrix(ChunkKey key) {
        var modelMatrix = Mat4.of(0);
        modelMatrix.translation(
                key.x() * CHUNK_SIZE,
                key.y() * CHUNK_SIZE,
                key.z() * CHUNK_SIZE
        );
        return modelMatrix;
    }

    // TODO temporary
    public void draw() {

        for (var entry : chunkMap.entrySet()) {
            var optionalData = mesher.getData(entry.getKey());

            this.modelMatrix.set(entry.getValue().modelMatrix);

            shaderManager.use("simple", uniforms);

            optionalData.ifPresent(data -> {
                glBindVertexArray(data.vao());
                glDrawElements(GL_TRIANGLES, data.indexCount(), GL_UNSIGNED_SHORT, 0L);
            });
        }

        glBindVertexArray(0);
    }
}
