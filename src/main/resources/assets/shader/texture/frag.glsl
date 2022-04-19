#version 330 core

in vec2 fTexCoords;
in vec4 fColor;
in float fTexture;

uniform sampler2D uTexture[16];

out vec4 color;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    color = texture(uTexture[int(fTexture)], fTexCoords) * vec4(hsv2rgb(fColor.rgb), fColor.a);
}
