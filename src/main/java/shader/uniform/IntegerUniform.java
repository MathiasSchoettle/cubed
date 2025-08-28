package shader.uniform;

import java.util.function.Supplier;

import static org.lwjgl.opengl.GL20.*;

public record IntegerUniform(Supplier<Integer> supplier) implements Uniform {
    @Override
    public void bind(int location) {
        glUniform1i(location, supplier.get());
    }
}