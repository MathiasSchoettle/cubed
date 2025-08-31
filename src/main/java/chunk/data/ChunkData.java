package chunk.data;

import math.mat.Mat4;

public class ChunkData {
    public final Chunk chunk;
    public final Mat4 modelMatrix;
    public final ChunkData[] neighbours = new ChunkData[6];
    public boolean needsRemesh = true;

    public ChunkData(Chunk chunk, Mat4 modelMatrix) {
        this.modelMatrix = modelMatrix;
        this.chunk = chunk;
    }
}
