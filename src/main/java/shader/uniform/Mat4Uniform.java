package shader.uniform;

import math.mat.Mat4;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public record Mat4Uniform(Mat4 matrix) implements Uniform {
    @Override
    public void bind(int location) {
        glUniformMatrix4fv(location, false, matrix.values);
    }
}