#version 330 core

in vec2 fTexCoords[7];

uniform sampler2D uTexture;
uniform float uWeights[7];

out vec4 color;

void main() {
    color = vec4(0);
    color += texture(uTexture, fTexCoords[0]) * uWeights[0];
    color += texture(uTexture, fTexCoords[1]) * uWeights[1];
    color += texture(uTexture, fTexCoords[2]) * uWeights[2];
    color += texture(uTexture, fTexCoords[3]) * uWeights[3];
    color += texture(uTexture, fTexCoords[4]) * uWeights[4];
    color += texture(uTexture, fTexCoords[5]) * uWeights[5];
    color += texture(uTexture, fTexCoords[6]) * uWeights[6];
    color.a = 1;
}
