package math.mat;

import math.vec.Vec3;

import java.util.Arrays;

public class Mat4 {

    // m x n, m rows, n columns
    // 0, 4,  8, 12
    // 1, 5,  9, 13
    // 2, 6, 10, 14
    // 3, 7, 11, 15
    public float[] values = new float[4 * 4];

    private Mat4() {}

    public void identity() {
        Arrays.fill(values, 0);
        values[0] = 1;
        values[5] = 1;
        values[10] = 1;
        values[15] = 1;
    }

    public void set(Mat4 other) {
        System.arraycopy(other.values, 0, this.values, 0, 16);
    }

    public static Mat4 of(float value) {
        var mat = new Mat4();
        Arrays.fill(mat.values, value);
        return mat;
    }

    public void perspective(float fov, float aspectRatio, float near, float far) {
        identity();

        float tangent = (float) Math.tan(fov / 2);
        float right = near * tangent;
        float top = right / aspectRatio;

        values[0] = near / right;
        values[5] = near / top;
        values[10] = -(far + near) / (far - near);
        values[11] = -1;
        values[14] = -(2 * far * near) / (far - near);
        values[15] = 0;
    }

    public void translation(float x, float y, float z) {
        identity();
        values[12] = x;
        values[13] = y;
        values[14] = z;
    }


    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
