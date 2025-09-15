package environment;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Cubemap {

    private final int vao, vbo, ibo;

    public Cubemap() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ibo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void render() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
    }

    private static final float[] VERTICES = new float[]{
            -1, -1, 1,
            1, -1, 1,
            1, -1, -1,
            -1, -1, -1,
            -1, 1, 1,
            1, 1, 1,
            1, 1, -1,
            -1, 1, -1,
    };
    private static final int[] INDICES = new int[]{
            1,6,2,  6,1,5, // right
            0,7,4,  7,0,3, // left
            4,6,5,  6,4,7, // top
            0,2,3,  2,0,1, // bottom
            0,5,1,  5,0,4, // back
            3,6,7,  6,3,2 // front
    };
}
