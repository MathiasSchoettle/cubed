package block.meta;

// TODO better name for this
public record DetailedTextureMeta(
        String front,
        String back,
        String left,
        String right,
        String top,
        String bottom
) implements TextureMeta {
}
