package shader.uniform;

import math.mat.Mat4;
import math.vec.Vec3;

import java.util.HashMap;
import java.util.Map;

public class Uniforms {

    public final Map<String, Uniform> uniforms = new HashMap<>();

    public void mat4(String name, Mat4 value) {
        uniforms.put(name, new Mat4Uniform(value));
    }

    public void vec3(String name, Vec3 value) {
        uniforms.put(name, new Vec3Uniform(value));
    }
}
