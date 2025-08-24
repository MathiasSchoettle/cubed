package shader.uniform;

import math.vec.Vec3;

import static org.lwjgl.opengl.GL20.glUniform3f;

public record Vec3Uniform(Vec3 vector) implements Uniform {
    @Override
    public void bind(int location) {
        glUniform3f(location, vector.x, vector.y, vector.z);
    }
}
