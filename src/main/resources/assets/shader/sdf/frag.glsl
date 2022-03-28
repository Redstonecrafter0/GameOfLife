in vec4 fColor;
in vec2 fTexCoords;

uniform sampler2D uTexture;

out vec4 color;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    float upperPointCutoff = 0.5;
    float midpointCutoff = 0.49;
    float c = texture(uFontTexture, fTexCoords).r;
    if (c > upperPointCutoff){
        color = vec4(hsv2rgb(fColor.rgb), fColor.a);
    } else if (c > midpointCutoff) {
        float smoothC = smoothstep(midpointCutoff, upperPointCutoff, c);
        color = vec4(1, 1, 1, smoothC) * vec4(hsv2rgb(fColor.rgb), fColor.a);
    } else {
        color = vec4(0, 0, 0, 0);
    }
}
