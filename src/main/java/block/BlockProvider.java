package block;

import block.meta.*;

import java.util.*;

public class BlockProvider {
    // all registered block info records
    private final Map<String, BlockInfo> blockInfos = new HashMap<>();
    // list of texture file names, index corresponds to position in 2D texture array
    private final List<String> textures = new ArrayList<>();

    private static final String INVALID_BLOCK_NAME = "_INVALID";
    private static final String INVALID_BLOCK_TEXTURE = "invalid.png";
    // index of invalid texture should always be 0
    private static final int[] INVALID_TEXTURE_OFFSETS = new int[] { 0, 0, 0, 0, 0, 0 };
    private static final BlockInfo INVALID_BLOCK_INFO = new BlockInfo(
            -1,
            INVALID_BLOCK_NAME,
            INVALID_TEXTURE_OFFSETS,
            false,
            false
    );

    public BlockProvider(List<BlockMeta> blockData) {
        // add invalid texture as first texture, will be used if requested texture does not exist
        addTexture(INVALID_BLOCK_TEXTURE);

        for (var data : blockData) {
            // register distinct textures in arraylist for future indexing.
            switch (data.texture()) {
                case AllTextureMeta t -> addTexture(t.all());
                case DefaultTextureMeta t -> addTexture(t.top(), t.side(), t.bottom());
                case DetailedTextureMeta t -> addTexture(t.front(), t.back(), t.left(), t.right(), t.top(), t.bottom());
                case NoTextureMeta ignore -> {}
            }
        }

        // create block infos
        int id = 0;
        for (var data : blockData) {
            var blockInfo = new BlockInfo(
                    id++,
                    data.name(),
                    getTextureOffsets(data.texture()),
                    data.opaque(),
                    data.hasMesh()
            );

            blockInfos.put(data.name(), blockInfo);
        }
    }

    private int[] getTextureOffsets(TextureMeta textureMeta) {
        return switch (textureMeta) {
            case AllTextureMeta t -> {
                var index = textures.indexOf(t.all());
                yield new int[] { index, index, index, index, index, index };
            }
            case DefaultTextureMeta t -> {
                var topIndex = textures.indexOf(t.top());
                var sideIndex = textures.indexOf(t.side());
                var bottomIndex = textures.indexOf(t.bottom());
                yield new int[] { sideIndex, sideIndex, topIndex, bottomIndex, sideIndex, sideIndex };
            }
            case DetailedTextureMeta t -> {
                var topIndex = textures.indexOf(t.top());
                var bottomIndex = textures.indexOf(t.bottom());
                var leftIndex = textures.indexOf(t.left());
                var rightIndex = textures.indexOf(t.right());
                var frontIndex = textures.indexOf(t.front());
                var backIndex = textures.indexOf(t.back());
                yield new int[] {frontIndex, backIndex, topIndex, bottomIndex, leftIndex, rightIndex};
            }
            case NoTextureMeta ignore -> INVALID_TEXTURE_OFFSETS;
        };
    }

    private void addTexture(String ...textures) {
        Arrays.stream(textures)
                .filter(texture -> !this.textures.contains(texture))
                .forEach(this.textures::add);
    }

    public BlockInfo getBlockInfo(String name) {
        return blockInfos.getOrDefault(name, INVALID_BLOCK_INFO);
    }

    public List<String> getTextures() {
        return Collections.unmodifiableList(textures);
    }
}
