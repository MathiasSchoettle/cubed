package block;

import block.meta.BlockMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockProvider {

    private short currentIndex = 0;

    private final Map<String, AugmentedBlockData> blockData = new HashMap<>();

    // store texture offsets for each side of a given block id
    private final Map<Short, Short[]> textureOffsets = new HashMap<>();

    private static final short INVALID_BLOCK_ID = -1;

    public BlockProvider(List<BlockMeta> blockData) {


    }

    public void register(BlockMeta blockMeta) {
        blockData.put(blockMeta.name(), new AugmentedBlockData(currentIndex, blockMeta));
        currentIndex++;
    }

    public short getBlockId(String name) {
         var data = blockData.get(name);
         return data != null ? data.id : INVALID_BLOCK_ID;
    }

    public int getTextureIndex(short blockId) {
        return 1;
    }

    private record AugmentedBlockData(
            short id,
            BlockMeta data
    ) {}
}
