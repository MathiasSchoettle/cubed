package chunk.data;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    public static final int SLICE_SIZE = CHUNK_SIZE * CHUNK_SIZE;
    public static final int BLOCK_COUNT = SLICE_SIZE * CHUNK_SIZE;

    public final byte[] blockData = new byte[BLOCK_COUNT];

    public byte get(int x, int y, int z) {
        assert x >= 0 && x < CHUNK_SIZE;
        assert y >= 0 && y < CHUNK_SIZE;
        assert z >= 0 && z < CHUNK_SIZE;
        return blockData[x + y * CHUNK_SIZE + z * SLICE_SIZE];
    }

    public void set(int x, int y, int z, byte value) {
        assert x >= 0 && x < CHUNK_SIZE;
        assert y >= 0 && y < CHUNK_SIZE;
        assert z >= 0 && z < CHUNK_SIZE;
        blockData[x + y * CHUNK_SIZE + z * SLICE_SIZE] = value;
    }

    public boolean trySet(int x, int y, int z, byte value) {
        if (
            x >= 0 && x < CHUNK_SIZE &&
            y >= 0 && y < CHUNK_SIZE &&
            z >= 0 && z < CHUNK_SIZE
        ) {
            set(x, y, z, value);
            return true;
        }
        return false;
    }

    public static Chunk copy(Chunk chunk) {
        var newChunk = new Chunk();
        System.arraycopy(chunk.blockData, 0, newChunk.blockData, 0, chunk.blockData.length);
        return newChunk;
    }
}
