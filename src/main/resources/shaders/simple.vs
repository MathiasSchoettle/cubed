#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec3 norm;
layout (location=2) in vec2 textureCoordinates;

uniform mat4 view;
uniform mat4 projection;

out vec2 tc;
out vec3 normal;

void main()
{
    tc = textureCoordinates;
    normal = norm;
    gl_Position = projection * view * vec4(position, 1.0);
}