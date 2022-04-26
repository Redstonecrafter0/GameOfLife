#version 330 core

layout (location=0) in vec2 aPos1;
layout (location=1) in vec2 aPos2;
layout (location=2) in vec2 aControl1;
layout (location=3) in vec2 aControl2;
layout (location=4) in vec4 aColor1;
layout (location=5) in vec4 aColor2;
layout (location=6) in float aWidth;
layout (location=7) in float aSegments;

out VertexData {
    vec2 pos1;
    vec2 pos2;
    vec2 control1;
    vec2 control2;
    vec4 color1;
    vec4 color2;
    float width;
    int segments;
} vOut;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

void main() {
    vOut.pos1 = aPos1;
    vOut.pos2 = aPos2;
    vOut.control1 = aControl1;
    vOut.control2 = aControl2;
    vOut.color1 = vec4(rgb2hsv(aColor1.rgb), aColor1.a);
    vOut.color2 = vec4(rgb2hsv(aColor2.rgb), aColor2.a);
    vOut.width = aWidth;
    vOut.segments = int(aSegments);
}
