package chunk.generate;

import java.util.HashMap;
import java.util.Map;

public class ChunkProviderBuilder {

    private final ChunkCache cache = new ChunkCache();

    private final Map<String, StageValue> values = new HashMap<>();

    public ChunkProviderBuilder addStage(String name, ChunkStage stage) {
        if (values.containsKey(name)) {
            throw new RuntimeException("Stage with name " + name + " was registered twice");
        }

        var newProvider = new ChunkProvider(name, new HashMap<>(values), cache);
        values.put(name, new StageValue(stage, newProvider));

        return this;
    }
}
