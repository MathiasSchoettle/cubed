package shader.uniform;

public sealed interface Uniform permits Mat4Uniform, Vec3Uniform, IntegerUniform {
    void bind(int location);
}
