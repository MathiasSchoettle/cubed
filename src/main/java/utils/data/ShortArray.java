package utils.data;

import java.util.Arrays;

public class ShortArray {
    public short[] data;
    private int length = 0;

    public ShortArray() {
        data = new short[0];
    }

    public void init(int size) {
        data = new short[size];
    }

    public ShortArray push(short value) {
        if (length == data.length) {
            data = Arrays.copyOf(data, (data.length + 1) * 2 * 2);
        }
        data[length++] = value;
        return this;
    }

    public ShortArray push(int value) {
        return push((short) value);
    }

    public int size() {
        return length;
    }

    public void clear() {
        length = 0;
    }
}
