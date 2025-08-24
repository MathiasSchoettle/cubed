package input;

@FunctionalInterface
public interface MouseMoveCallback {
    void apply(float x, float y, float dx, float dy);
}
