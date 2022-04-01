in vec4 fColor;
in vec4 fBgColor;
in vec2 fTexCoords;

uniform sampler2D uTexture;
uniform float uScreenPxRange;

out vec4 color;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 rColor = vec4(hsv2rgb(fColor.rgb), fColor.a);
    vec4 rBgColor = vec4(hsv2rgb(fBgColor.rgb), fBgColor.a);
    vec3 msd = texture(uTexture, fTexCoords).rgb;
    float sd = median(msd.r, msd.g, msd.b);
    float screenPxDistance = uScreenPxRange * (sd - 0.5);
    float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);
    color = mix(rBgColor, rColor, opacity);
}
