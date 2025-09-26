package block.meta;

public record BlockInfo(
        int id, // TODO check if we actually need this
        String name,
        // store texture indices for each side of a given block id
        // +X, -X, +Y, -Y, +Z, -Z
        int[] textureIndices,
        boolean opaque,
        boolean hasMesh
) {
    public int getTextureIndex(int direction) {
        return textureIndices[direction];
    }
}
