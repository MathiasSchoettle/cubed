package math.vec;

public class IVec3 {
    public int x, y, z;

    private IVec3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static IVec3 of(int x, int y, int z) {
        return new IVec3(x, y, z);
    }

    public static IVec3 of(int value) {
        return new IVec3(value, value, value);
    }

    public void set(IVec3 other) {
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public boolean equals(IVec3 other) {
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public String toString() {
        return "IVec3[x: " + x + ", y: " + y + ", z: " + z + "]";
    }
}
