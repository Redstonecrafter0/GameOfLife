#version 330 core

layout (location=0) in vec2 aPos;
layout (location=1) in vec2 aTexCoords;
layout (location=2) in vec4 aColor;
layout (location=3) in float aTexture;

uniform mat4 uProjectionMatrix;

out vec2 fTexCoords;
out vec4 fColor;
out float fTexture;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

void main() {
    fTexCoords = aTexCoords;
    fColor = vec4(rgb2hsv(aColor.rgb), aColor.a);
    fTexture = aTexture;
    gl_Position = uProjectionMatrix * vec4(aPos, 1, 1);
}
