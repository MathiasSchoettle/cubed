package utils;

import java.util.Arrays;

public class FloatArray {
    public float[] data;
    private int length = 0;

    public FloatArray() {
        data = new float[0];
    }

    public void init(int size) {
        data = new float[size];
    }

    public FloatArray push(float value) {
        if (length == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[length++] = value;
        return this;
    }

    public FloatArray push(double value) {
        return push((float) value);
    }

    public int size() {
        return length;
    }
}
