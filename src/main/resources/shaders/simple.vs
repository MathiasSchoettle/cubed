#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec3 norm;
layout (location=2) in vec2 textureCoordinates;

uniform mat4 view;
uniform mat4 model;
uniform mat4 projection;

out vec2 tc;
out vec3 normal;
out float layer;

void main()
{
    tc = textureCoordinates;
    normal = norm;
    layer = 0;
    gl_Position = projection * view * model * vec4(position, 1.0);
}