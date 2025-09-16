package chunk.generate;

import chunk.data.Chunk;
import chunk.data.ChunkKey;

import java.util.Map;

public class ChunkProvider {

    private final String stageName;

    private final Map<String, StageValue> stages;

    private final ChunkCache cache;

    public ChunkProvider(String stageName, Map<String, StageValue> stages, ChunkCache cache) {
        this.stageName = stageName;
        this.stages = stages;
        this.cache = cache;
    }

    public Chunk get(String stage, ChunkKey chunkKey, int dx, int dy, int dz) {
        if (!stages.containsKey(stage)) {
            throw new RuntimeException("Stage " + stageName + " can not access chunkStage " + stage);
        }

        int x = chunkKey.x() + dx;
        int y = chunkKey.y() + dy;
        int z = chunkKey.z() + dz;

        var cachedChunk = cache.get(stage, x, y, z);
        if (cachedChunk.isPresent()) {
            return cachedChunk.get();
        }

        var value = stages.get(stage);
        var chunk = value.chunkStage().generate(x, y, z, value.provider());

        cache.put(chunk, x, y, z);

        return chunk;
    }
}
