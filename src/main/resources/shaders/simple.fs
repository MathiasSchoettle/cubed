#version 450

out vec4 fragColor;

in vec2 tc;
in vec3 normal;

uniform sampler2D out_tex;
uniform vec3 color;

void main()
{
    vec4 tex = texture(out_tex, tc);
    fragColor = mix(tex, vec4(color, 1), 0.2);
}