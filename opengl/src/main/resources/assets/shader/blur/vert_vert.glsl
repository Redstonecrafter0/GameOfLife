#version 330 core

in vec2 aPos;
in vec2 aTexCoords;

uniform mat4 uProjectionMatrix;
uniform float uSize;

out vec2 fTexCoords[7];

void main() {
    gl_Position = uProjectionMatrix * vec4(aPos, 1, 1);
    for (int i = -3; i <= 3; i++) {
        fTexCoords[i + 3] = aTexCoords + vec2(0, uSize * i);
    }
}
