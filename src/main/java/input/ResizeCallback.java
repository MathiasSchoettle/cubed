package input;

@FunctionalInterface
public interface ResizeCallback {
    void apply(int width, int height);
}
