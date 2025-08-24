package math.vec;

public class Vec3 {
    public float x, y, z;

    private Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3 of(float x, float y, float z) {
        return new Vec3(x, y, z);
    }

    public static Vec3 of(float value) {
        return new Vec3(value, value, value);
    }

    public Vec3 add(Vec3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vec3 cross(Vec3 first, Vec3 second) {
        x = first.y * second.z - second.y * first.z;
        y = first.z * second.x - second.z * first.x;
        z = first.x * second.y - second.x * first.y;
        return this;
    }

    public Vec3 invert() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3 rotate(Vec3 axis, float angle) {

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        var term1 = this.scale(cos);
        var term2 = Vec3.of(0).cross(axis, this).scale(sin);
        var term3 = axis.getScaled(axis.dot(this) * (1 - cos));

        term1.add(term2).add(term3);

        x = term1.x;
        y = term1.y;
        z = term1.z;

        return this;
    }

    public float dot(Vec3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec3 scale(float factor) {
        x *= factor;
        y *= factor;
        z *= factor;
        return this;
    }

    public Vec3 getScaled(float factor) {
        return Vec3.of(
                x * factor,
                y * factor,
                z * factor
        );
    }

    public Vec3 normalize() {
        var factor = (float) Math.sqrt(x * x + y * y + z * z);
        x /= factor;
        y /= factor;
        z /= factor;
        return this;
    }

    @Override
    public String toString() {
        return "Vec3[x: " + x + ", y: " + y + ", z: " + z + "]";
    }
}
