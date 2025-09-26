package block.meta;

public record BlockMeta(
        String name,
        boolean opaque,
        boolean hasMesh,
        TextureMeta texture
) {}
