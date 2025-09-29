package chunk.generate;

import chunk.data.Chunk;
import utils.data.LeastRecentlyUsedCache;
import utils.data.Tuple;

import java.util.Optional;

public class ChunkCache {

    private final LeastRecentlyUsedCache<Tuple<String, ChunkPosition>, Chunk> cache = new LeastRecentlyUsedCache<>(256);

    public Optional<Chunk> get(String stage, ChunkPosition position) {
        Chunk chunk = cache.get(new Tuple<>(stage, position));
        return Optional.ofNullable(chunk);
    }

    public void store(Chunk chunk, String stage, ChunkPosition position) {
        cache.put(new Tuple<>(stage, position), chunk);
    }
}
