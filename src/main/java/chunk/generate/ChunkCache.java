package chunk.generate;

import chunk.data.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO implement
public class ChunkCache {

    private final Map<String, Map<ChunkPosition, Chunk>> chunks = new HashMap<>();

    public Optional<Chunk> get(String stage, ChunkPosition position) {
        return Optional.empty();
    }

    public void store(Chunk chunk, String stage, ChunkPosition position) {

    }
}
