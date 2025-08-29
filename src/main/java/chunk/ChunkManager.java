package chunk;

import chunk.data.Chunk;
import chunk.data.ChunkKey;

import java.util.HashMap;
import java.util.Map;

// TODO implement
public class ChunkManager {

    private final ChunkStorage storage;

    private final ChunkGenerator generator;

    private final ChunkMesher mesher;

    private final Map<ChunkKey, Chunk> chunkMap = new HashMap<>();

    public ChunkManager(ChunkStorage storage, ChunkGenerator generator, ChunkMesher mesher) {
        this.storage = storage;
        this.generator = generator;
        this.mesher = mesher;
    }

    private Chunk load(ChunkKey key) {
        var chunk = storage.load(key).orElseGet(() -> generator.generate(key));
        chunkMap.put(key, chunk);
        return chunk;
    }

    private void unload(ChunkKey key) {
        var chunk = chunkMap.remove(key);
        storage.persist(key, chunk);
    }
}
