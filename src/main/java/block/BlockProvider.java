package block;

import block.meta.AllTextureMeta;
import block.meta.BlockMeta;
import block.meta.DefaultTextureMeta;
import block.meta.NoTextureMeta;

import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

public class BlockProvider {

    private final Map<String, AugmentedBlockData> augmentedData = new HashMap<>();

    // list of texture file names, index corresponds to position in 2D texture array
    private final List<String> textures = new ArrayList<>();

    // store texture offsets for each side of a given block id
    // +X, -X, +Y, -Y, +Z, -Z
    private final Map<Short, short[]> textureOffsets;

    private static final short INVALID_BLOCK_ID = -1;
    private static final String INVALID_BLOCK_TEXTURE = "invalid.png";
    // index of invalid texture should always be 0
    private final short[] INVALID_TEXTURE_OFFSETS = new short[] { 0, 0, 0, 0, 0, 0 };

    public BlockProvider(List<BlockMeta> blockData) {

        // add invalid texture as first texture, will be used if requested texture does not exist
        addTexture(INVALID_BLOCK_TEXTURE);

        short currentIndex = 0;
        for (var data : blockData) {
            // create augmented data including runtime id for referencing
            var augmented = new AugmentedBlockData(currentIndex++, data);
            augmentedData.put(data.name(), augmented);

            // register distinct textures in arraylist for future indexing.
            switch (data.texture()) {
                case AllTextureMeta t -> addTexture(t.all());
                case DefaultTextureMeta t -> addTexture(t.top(), t.side(), t.bottom());
                case NoTextureMeta ignore -> {}
            }
        }

        // build texture offsets
        textureOffsets = new HashMap<>(blockData.size());

        // add invalid texture mapping
        short invalidIndex = (short) textures.indexOf(INVALID_BLOCK_TEXTURE);
        var invalidMappings = new short[6];
        Arrays.fill(invalidMappings, invalidIndex);
        textureOffsets.put(INVALID_BLOCK_ID, invalidMappings);

        // add block side offsets
        for (var data : augmentedData.values()) {
            var offsets = getTextureOffsets(data);
            textureOffsets.put(data.id, offsets);
        }
    }

    private short[] getTextureOffsets(AugmentedBlockData blockData) {
        return switch (blockData.data.texture()) {
            case AllTextureMeta t -> {
                var index = (short) textures.indexOf(t.all());
                yield new short[] { index, index, index, index, index, index };
            }
            case DefaultTextureMeta t -> {
                var topIndex = (short) textures.indexOf(t.top());
                var sideIndex = (short) textures.indexOf(t.side());
                var bottomIndex = (short) textures.indexOf(t.bottom());
                yield new short[] { sideIndex, sideIndex, topIndex, bottomIndex, sideIndex, sideIndex };
            }
            case NoTextureMeta ignore -> INVALID_TEXTURE_OFFSETS;
        };
    }

    private void addTexture(String ...textures) {
        Arrays.stream(textures)
                .filter(texture -> !this.textures.contains(texture))
                .forEach(this.textures::add);
    }

    public short getBlockId(String name) {
         var data = augmentedData.get(name);
         return data != null ? data.id : INVALID_BLOCK_ID;
    }

    public int getTextureIndex(short blockId, int direction) {
        short[] offsets = textureOffsets.getOrDefault(blockId, INVALID_TEXTURE_OFFSETS);
        return offsets[direction];
    }

    public List<String> getTextures() {
        return Collections.unmodifiableList(textures);
    }

    private record AugmentedBlockData(
            short id,
            BlockMeta data
    ) {}
}
