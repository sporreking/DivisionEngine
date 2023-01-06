#version 430

// Uniforms
layout(location=2003) uniform vec4 color;
layout(location=2004) uniform sampler2D tex;

layout(location=3000) uniform bool useTexture;

// Attributes
in vec2 pass_TexCoords;

out vec4 out_Color;
void main() {
    if (useTexture)
        out_Color = vec4(texture(tex, pass_TexCoords).xyz, 1);
    else out_Color = color;
}