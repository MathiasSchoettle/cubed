package math.noise;

import java.util.Random;

public class PerlinNoise3D {

    private final int[] p; // Permutation table

    public PerlinNoise3D(long seed) {
        p = new int[512];
        int[] perm = new int[256];
        Random random = new Random(seed);

        // Initialize with values 0..255
        for (int i = 0; i < 256; i++) {
            perm[i] = i;
        }

        // Shuffle using Fisher-Yates
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }

        // Duplicate the permutation table
        for (int i = 0; i < 512; i++) {
            p[i] = perm[i & 255];
        }
    }

    // Default constructor with random seed
    public PerlinNoise3D() {
        this(System.nanoTime());
    }

    // Fade function improves interpolation smoothness
    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear interpolation
    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    // Gradient function
    private static float grad(int hash, float x, float y, float z) {
        int h = hash & 15;
        float u = (h < 8) ? x : y;
        float v = (h < 4) ? y : (h == 12 || h == 14 ? x : z);
        return (((h & 1) == 0) ? u : -u) + (((h & 2) == 0) ? v : -v);
    }

    // Get noise value for (x,y,z) normalized to [0,1]
    public float getNoise(float x, float y, float z) {
        // Find unit cube
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        // Relative position inside cube
        x -= (int) Math.floor(x);
        y -= (int) Math.floor(y);
        z -= (int) Math.floor(z);

        // Fade curves
        float u = fade(x);
        float v = fade(y);
        float w = fade(z);

        // Hash coordinates
        int A  = p[X] + Y,    AA = p[A] + Z,    AB = p[A + 1] + Z;
        int B  = p[X + 1] + Y, BA = p[B] + Z,   BB = p[B + 1] + Z;

        // Interpolate gradients (raw noise in [-1,1])
        float raw = lerp(w,
                lerp(v,
                        lerp(u,
                                grad(p[AA], x, y, z),
                                grad(p[BA], x - 1, y, z)),
                        lerp(u,
                                grad(p[AB], x, y - 1, z),
                                grad(p[BB], x - 1, y - 1, z))),
                lerp(v,
                        lerp(u,
                                grad(p[AA + 1], x, y, z - 1),
                                grad(p[BA + 1], x - 1, y, z - 1)),
                        lerp(u,
                                grad(p[AB + 1], x, y - 1, z - 1),
                                grad(p[BB + 1], x - 1, y - 1, z - 1)))
        );

        // Normalize to [0,1]
        return (raw + 1) * 0.5f;
    }

    // Octave Perlin for fractal noise normalized to [0,1]
    public float octaveNoise(float x, float y, float z, int octaves, float persistence) {
        float total = 0;
        float max = 0;
        float amplitude = 1;
        float frequency = 1;

        for (int i = 0; i < octaves; i++) {
            total += getNoise(x * frequency, y * frequency, z * frequency) * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }

        return total / max; // already in [0,1]
    }
}
