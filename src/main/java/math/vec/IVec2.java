package math.vec;

public class IVec2 {
    public int x, y;

    private IVec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static IVec2 of(int x, int y) {
        return new IVec2(x, y);
    }

    public static IVec2 of(int value) {
        return new IVec2(value, value);
    }
}
