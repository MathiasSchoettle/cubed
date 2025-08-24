package chunk;

import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Chunk {

    private static final float CUBE_SIZE = 1.0f;

    private static final int CHUNK_SIZE = 16;

    private static final int SLICE_SIZE = CHUNK_SIZE * CHUNK_SIZE;

    private static final int BLOCK_COUNT = SLICE_SIZE * CHUNK_SIZE;

    private final boolean[] blocks = new boolean[BLOCK_COUNT];

    public final List<Float> vertices = new ArrayList<>(100);

    public int vaoId;

    private float globalX, globalY, globalZ;

    public Chunk() {
        var rand = new Random();
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = rand.nextFloat() > ((float) i / blocks.length);
            blocks[i] = true;
        }
    }

    public void mesh() {

        int xcount = 0;

        for (int x = 0; x < CHUNK_SIZE; ++x) {
            for (int y = 0; y < CHUNK_SIZE; ++y) {
                for (int z = 0; z < CHUNK_SIZE; ++z) {
                    if (!hasBlock(x, y, z)) {
                        continue;
                    }

                    globalX = x;
                    globalY = y;
                    globalZ = z;

                    if (!hasBlock(x - 1, y, z)) { // left
                        xcount++;
                        addVertex(0, 0, 0);
                        addVertex(0, 1, 0);
                        addVertex(0, 1, 1);

                        addVertex(0, 0, 0);
                        addVertex(0, 1, 1);
                        addVertex(0, 0, 1);
                    }

                    if (!hasBlock(x + 1, y, z)) { // right
                        addVertex(1, 0, 0);
                        addVertex(1, 1, 0);
                        addVertex(1, 1, 1);

                        addVertex(1, 0,  0);
                        addVertex(1, 1,  1);
                        addVertex(1, 0,  1);
                    }

                    if (!hasBlock(x, y, z + 1)) { // back
                        addVertex(0, 0, 1);
                        addVertex(1, 0, 1);
                        addVertex(1, 1, 1);

                        addVertex(0, 0,  1);
                        addVertex(1, 1,  1);
                        addVertex(0, 1,  1);
                    }

                    if (!hasBlock(x, y, z - 1)) { // front
                        addVertex(0, 0, 0);
                        addVertex(1, 0, 0);
                        addVertex(1, 1, 0);

                        addVertex(0, 0,  0);
                        addVertex(1, 1,  0);
                        addVertex(0, 1,  0);
                    }

                    if (!hasBlock(x, y + 1, z)) { // top
                        addVertex(0, 1, 0);
                        addVertex(1, 1, 0);
                        addVertex(1, 1, 1);

                        addVertex(0, 1, 0);
                        addVertex(1, 1, 1);
                        addVertex(0, 1, 1);
                    }

                    if (!hasBlock(x, y - 1, z)) { // bottom
                        addVertex(0, 0, 0);
                        addVertex(1, 0, 0);
                        addVertex(1, 0, 1);

                        addVertex(0, 0, 0);
                        addVertex(1, 0, 1);
                        addVertex(0, 0, 1);
                    }
                }
            }
        }

        System.out.println(xcount);

        var array = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); ++i) {
            array[i] = vertices.get(i);
        }

        var buffer = MemoryUtil.memAllocFloat(vertices.size());
        buffer.put(array).flip();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        var vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        memFree(buffer);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void addVertex(float x, float y, float z) {
        vertices.add(globalX + x);
        vertices.add(globalY + y);
        vertices.add(globalZ + z);
    }

    private boolean hasBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE ||
                y < 0 || y >= CHUNK_SIZE ||
                z < 0 || z >= CHUNK_SIZE) {
            return false;
        }

        int index = x + z * CHUNK_SIZE + y * SLICE_SIZE;
        return blocks[index];
    }
}
