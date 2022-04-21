#version 330 core

in vec2 aPos;

uniform mat4 uProjectionMatrix;
uniform float uSize;
uniform int uDirection;

out vec2 fTexCoords[7];

void main() {
    gl_Position = uProjectionMatrix * vec4(aPos, 0, 1);
    vec2 centerTexCoords = aPos * 0.5 + 0.5;
    float pixelSize = 1f / uSize;
    if (uDirection == 0) {
        for (int i = -3; i <= 3; i++) {
            fTexCoords[i + 3] = centerTexCoords + vec2(0, pixelSize * i);
        }
    } else {
        for (int i = -3; i <= 3; i++) {
            fTexCoords[i + 3] = centerTexCoords + vec2(pixelSize * i, 0);
        }
    }
}
