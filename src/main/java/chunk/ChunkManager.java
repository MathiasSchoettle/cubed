package chunk;

import chunk.data.Chunk;
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

    public Chunk load(ChunkKey key) {
        var chunk = storage.load(key).orElseGet(() -> generator.generate(key));
        chunkMap.put(key, new ChunkData(chunk, getModelMatrix(key)));
        return chunk;
    }

    public void unload(ChunkKey key) {
        var data = chunkMap.remove(key);
        mesher.remove(key);
        storage.persist(key, data.chunk);
    }

    public void mesh() {
        for (var entry : chunkMap.entrySet()) {
            var key = entry.getKey();
            var data = entry.getValue();

            Chunk[] neighbours = new Chunk[6];

            for (int x = -1; x <= 1; x+=2) {
                var neighbour = chunkMap.get(new ChunkKey(key.x() + x, key.y(), key.z()));
                if (neighbour != null) {
                    int index = (x + 1) / 2;
                    neighbours[index] = neighbour.chunk;
                }
            }

            for (int y = -1; y <= 1; y+=2) {
                var neighbour = chunkMap.get(new ChunkKey(key.x(), key.y() + y, key.z()));
                if (neighbour != null) {
                    int index = (y + 1) / 2;
                    neighbours[2 + index] = neighbour.chunk;
                }
            }

            for (int z = -1; z <= 1; z+=2) {
                var neighbour = chunkMap.get(new ChunkKey(key.x(), key.y(), key.z() + z));
                if (neighbour != null) {
                    int index = (z + 1) / 2;
                    neighbours[4 + index] = neighbour.chunk;
                }
            }

            mesher.mesh(key, data.chunk, neighbours);
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

            this.modelMatrix.set(entry.getValue().modelMatrix());

            shaderManager.use("simple", uniforms);

            optionalData.ifPresent(data -> {
                glBindVertexArray(data.vao());
                glDrawElements(GL_TRIANGLES, data.indexCount(), GL_UNSIGNED_SHORT, 0L);
            });
        }

        glBindVertexArray(0);
    }

    // TODO: maybe store neighbours in array here as well? maybe even as map, with chunk key as key?
    private record ChunkData(
            Chunk chunk,
            Mat4 modelMatrix
    ) {}
}
