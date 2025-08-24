package shader.uniform;

public sealed interface Uniform permits Mat4Uniform, Vec3Uniform {
    void bind(int location);
}
