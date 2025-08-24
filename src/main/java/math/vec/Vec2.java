package math.vec;

public class Vec2 {
    public float x, y;

    private Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vec2 of(float x, float y) {
        return new Vec2(x, y);
    }

    public static Vec2 of(float value) {
        return new Vec2(value, value);
    }
}
