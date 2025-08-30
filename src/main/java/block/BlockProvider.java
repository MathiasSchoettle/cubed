package block;

import java.util.HashMap;
import java.util.Map;

public class BlockProvider {

    public static final short INVALID_BLOCK_ID = 0;

    private short currentIndex = 1;

    private final Map<String, Short> blockData = new HashMap<>();

    public void register(String name) {
        blockData.put(name, currentIndex);
        currentIndex++;
    }

    public short getBlockId(String name) {
        return blockData.getOrDefault(name, INVALID_BLOCK_ID);
    }
}
