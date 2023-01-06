#version 430

// Uniforms
layout(location=2003) uniform vec4 color;
layout(location=2004) uniform sampler2D tex;

const float width = .5;
const float edge = .2;

// Attributes
in vec2 pass_TexCoords;

out vec4 out_Color;
void main() {
    float d = 1.0 - texture(tex, pass_TexCoords).a;
    float a = 1.0 - smoothstep(width, width + edge, d);
    out_Color = vec4(color.xyz, color.w * a);
}