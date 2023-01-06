#version 430

// Uniforms
layout(location=0) uniform mat4 modelMatrix;
layout(location=1) uniform mat4 viewMatrix;
layout(location=2) uniform mat4 projectionMatrix;

// Attributes
layout(location=0) in vec3 in_Position;
layout(location=2) in vec2 in_TexCoords;

out vec2 pass_TexCoords;
void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(in_Position.xy, 0, 1);
    pass_TexCoords = in_TexCoords;
}