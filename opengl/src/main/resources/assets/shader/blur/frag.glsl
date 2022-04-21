#version 330 core

in vec2 fTexCoords[7];

uniform sampler2D uTexture;

out vec4 color;

void main() {
    color = vec4(0);
    color += texture(uTexture, fTexCoords[0]) * 0.0029452;
    color += texture(uTexture, fTexCoords[1]) * 0.0466409;
    color += texture(uTexture, fTexCoords[2]) * 0.2417368;
    color += texture(uTexture, fTexCoords[3]) * 0.4173538;
    color += texture(uTexture, fTexCoords[4]) * 0.2417368;
    color += texture(uTexture, fTexCoords[5]) * 0.0466409;
    color += texture(uTexture, fTexCoords[6]) * 0.0029452;
    color.a = 1;
}
