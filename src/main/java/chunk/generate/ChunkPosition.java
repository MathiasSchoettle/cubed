package chunk.generate;

public record ChunkPosition(int x, int y, int z) {

    public ChunkPosition offsetX(int dx) {
        return offset(dx, 0, 0);
    }

    public ChunkPosition offsetY(int dy) {
        return offset(0, dy, 0);
    }

    public ChunkPosition offsetZ(int dz) {
        return offset(0, 0, dz);
    }

    public ChunkPosition offset(int dx, int dy, int dz) {
        return new ChunkPosition(x + dx, y + dy, z + dz);
    }
}
