#version 450

out vec4 fragColor;

in vec3 color;
in vec2 tc;

uniform sampler2D out_tex;

// simple hash from int → vec3 color
vec3 hashColor(int id) {
    uint h = uint(id) * 1664525u + 1013904223u; // LCG
    // Take three different mixes
    float r = float((h >> 16) & 255u) / 255.0;
    float g = float((h >> 8) & 255u) / 255.0;
    float b = float(h & 255u) / 255.0;
    return vec3(r, g, b);
}

void main()
{
    fragColor = vec4(hashColor(gl_PrimitiveID).xzz, 1.0);
    fragColor = vec4(color.xyz, 1);
    fragColor = texture(out_tex, tc);
}