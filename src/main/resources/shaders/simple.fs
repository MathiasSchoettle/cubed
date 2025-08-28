#version 450

out vec4 fragColor;

in vec2 tc;
in vec3 normal;
in float layer;

uniform sampler2DArray textures;
uniform vec3 color;

void main()
{
    vec4 tex = texture(textures, vec3(tc, layer));
    fragColor = tex;
}