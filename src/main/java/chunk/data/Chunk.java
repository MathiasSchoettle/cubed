package chunk.data;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int SLICE_SIZE = CHUNK_SIZE * CHUNK_SIZE;
    public static final int BLOCK_COUNT = SLICE_SIZE * CHUNK_SIZE;

    public final short[] blockData = new short[BLOCK_COUNT];

    public short get(int x, int y, int z) {
        assert x >= 0 && x < CHUNK_SIZE;
        assert y >= 0 && y < CHUNK_SIZE;
        assert z >= 0 && z < CHUNK_SIZE;
        return blockData[x + y * CHUNK_SIZE + z * SLICE_SIZE];
    }

    public void set(int x, int y, int z, short value) {
        assert x >= 0 && x < CHUNK_SIZE;
        assert y >= 0 && y < CHUNK_SIZE;
        assert z >= 0 && z < CHUNK_SIZE;
        blockData[x + y * CHUNK_SIZE + z * SLICE_SIZE] = value;
    }
}
