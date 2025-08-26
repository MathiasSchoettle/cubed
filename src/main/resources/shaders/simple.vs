#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in vec2 textureCoordinates;

uniform mat4 view;
uniform mat4 projection;

out vec3 color;
out vec2 tc;

void main()
{
    color = mix(vec3(textureCoordinates.xy, 1), normal, 0.5);
    tc = textureCoordinates;
    gl_Position = projection * view * vec4(position, 1.0);
}