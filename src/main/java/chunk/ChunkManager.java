package chunk;

import chunk.data.Chunk;
import chunk.data.ChunkData;
import chunk.data.ChunkKey;
import chunk.generate.ChunkProvider;
import chunk.generate.ChunkPosition;
import math.mat.Mat4;
import math.vec.IVec3;
import math.vec.Vec3;
import shader.ShaderManager;
import shader.uniform.Uniforms;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static chunk.data.Chunk.CHUNK_SIZE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class ChunkManager {

    private final ChunkStorage storage;

    private final ChunkProvider provider;

    private final Map<ChunkKey, Future<Chunk>> generatingChunks = new HashMap<>();

    private final ChunkMesher mesher;

    // TODO these following values are all very temporary
    //  we need a solution how to get uniforms and shader calls into something like a chunkRenderer, and how to
    //  and how to bind the current chunks model matrix to the uniforms object, as uniforms kind of want references
    private final Mat4 modelMatrix = Mat4.of(0);
    private final ShaderManager shaderManager;
    private final Uniforms uniforms;

    private final Map<ChunkKey, ChunkData> chunkMap = new HashMap<>();

    private final IVec3 chunkPosition = IVec3.of(0);
    private static final int RENDER_DISTANCE = 25;

    // TODO shader manager and uniforms are temporary
    public ChunkManager(ChunkStorage storage, ChunkProvider provider, ChunkMesher mesher, ShaderManager shaderManager, Uniforms uniforms) {
        this.storage = storage;
        this.provider = provider;
        this.mesher = mesher;
        this.shaderManager = shaderManager;
        this.uniforms = uniforms;
        this.uniforms.mat4("model", modelMatrix);
    }

    public void load(ChunkKey key) {
        if (chunkMap.containsKey(key) || generatingChunks.containsKey(key)) {
            return;
        }

        generatingChunks.put(key, provider.provide((new ChunkPosition(key.x(), key.y(), key.z()))));
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

    public void update(Vec3 position) {
        var newPos = IVec3.of(
                (int) (position.x / CHUNK_SIZE),
                (int) (position.y / CHUNK_SIZE),
                (int) (position.z / CHUNK_SIZE)
        );

        if (!chunkPosition.equals(newPos)) {

            chunkPosition.set(newPos);

            var toRemove = new HashSet<>(chunkMap.keySet().stream().toList());

            for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) for (int y = -RENDER_DISTANCE; y <= RENDER_DISTANCE; y++) for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                var key = new ChunkKey(chunkPosition.x + x, chunkPosition.y + y, chunkPosition.z + z);
                toRemove.remove(key);
            }

            toRemove.forEach(this::unload);

            for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) for (int y = -RENDER_DISTANCE; y <= RENDER_DISTANCE; y++) for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                var key = new ChunkKey(chunkPosition.x + x, chunkPosition.y + y, chunkPosition.z + z);

                // FIXME temporarily limit chunks to this height
                if (key.y() > -5 && key.y() < 5) {
                    load(key);
                }
            }
        }

        remesh();
    }

    public void remesh() {
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

        List<ChunkKey> completedKeys = new ArrayList<>();

        for (var entry : generatingChunks.entrySet()) {
            var future = entry.getValue();
            var key = entry.getKey();

            if (future.isDone()) {

                completedKeys.add(key);

                try {
                    var data = new ChunkData(future.get(), getModelMatrix(key));
                    setNeighbours(key, data);
                    chunkMap.put(key, data);
                } catch (InterruptedException | ExecutionException exception) {
                    System.out.println("Something went wrong while accessing a generated chunk + " + key + ": " + exception.getMessage());
                }
            }
        }

        completedKeys.forEach(generatingChunks::remove);

        mesher.tick();

        for (var entry : chunkMap.entrySet()) {
            var optionalData = mesher.getData(entry.getKey());

            optionalData.ifPresent(data -> {
                this.modelMatrix.set(entry.getValue().modelMatrix);
                shaderManager.use("simple", uniforms);
                glBindVertexArray(data.vao());
                glDrawElements(GL_TRIANGLES, data.indexCount(), GL_UNSIGNED_SHORT, 0L);
            });
        }

        glBindVertexArray(0);
    }
}
