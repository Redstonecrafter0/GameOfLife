#version 330 core

in BezierData {
    vec2 pos1;
    vec2 pos2;
    vec2 control1;
    vec2 control2;
} vOut;

vec2 bezier(float d, vec2 pos1, vec2 c1, vec2 pos2, vec2 c2) {
    vec2 b11 = mix(pos1, c1, d);
    vec2 b12 = mix(c1, c2, d);
    vec2 b13 = mix(c2, pos2, d);
    vec2 b21 = mix(b11, b12, d);
    vec2 b22 = mix(b12, b13, d);
    return mix(b21, b22, d);
}

void main() {

}
