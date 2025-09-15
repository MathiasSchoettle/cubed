#version 450
layout (location = 0) in vec3 pos;

out vec3 textureCoordinates;

uniform mat4 projection;
uniform mat4 view;

void main() {
    vec4 position = projection * view * vec4(pos, 1.0f);
    gl_Position = vec4(position.x, position.y, position.w, position.w);
    textureCoordinates = vec3(pos.x, pos.y, -pos.z);
}