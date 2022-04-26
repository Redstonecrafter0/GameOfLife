#version 330 core

layout (points) in;
layout (triangle_strip, max_vertices=3) out;

in VertexData {
    vec2 pos1;
    vec2 pos2;
    vec2 control1;
    vec2 control2;
    vec4 color1;
    vec4 color2;
    float width;
    int segments;
} vOut[1];

uniform mat4 uProjectionMatrix;

out VertexData {
    vec4 color;
} gOut;

vec2 bezierPoint(float d, vec2 pos1, vec2 c1, vec2 pos2, vec2 c2) {
    vec2 coef1 = (pos2 - (3.0 * c2) + (3.0 * c1) - pos1);
    vec2 coef2 = ((3.0 * c2) - (6.0 * c1) + (3.0 * pos1));
    vec2 coef3 = ((3.0 * c1) - (3.0 * pos1));
    vec2 coef4 = pos1;
    return (coef1 * d * d * d) + (coef2 * d * d) + (coef3 * d) + coef4;
}

vec2 bezierTangent(float d, vec2 pos1, vec2 c1, vec2 pos2, vec2 c2) {
    vec2 coef1 = (pos2 - (3.0 * c2) + (3.0 * c1) - pos1);
    vec2 coef2 = ((3.0 * c2) - (6.0 * c1) + (3.0 * pos1));
    vec2 coef3 = ((3.0 * c1) - (3.0 * pos1));
    return normalize((3.0 * coef1 * d * d) + (2.0 * coef2 * d) + coef3);
}

vec2 bezierNormal(float d, vec2 pos1, vec2 c1, vec2 pos2, vec2 c2) {
    vec2 tangent = bezierTangent(d, pos1, c1, pos2, c2);
    return vec2(tangent.y, -tangent.x);
}

void drawSegment(float d1, float d2, vec2 pos1, vec2 c1, vec2 pos2, vec2 c2, vec4 color1, vec4 color2, float width) {
    vec2 p11 = bezierPoint(d1, pos1, c1, pos2, c2) + (bezierNormal(d1, pos1, c1, pos2, c2) * width);
    vec2 p12 = bezierPoint(d1, pos1, c1, pos2, c2) - (bezierNormal(d1, pos1, c1, pos2, c2) * width);
    vec2 p21 = bezierPoint(d1, pos1, c1, pos2, c2) + (bezierNormal(d2, pos1, c1, pos2, c2) * width);
    vec2 p22 = bezierPoint(d1, pos1, c1, pos2, c2) - (bezierNormal(d2, pos1, c1, pos2, c2) * width);
    gl_Position = uProjectionMatrix * vec4(p11, 1, 1);
    gOut.color = color1;
    EmitVertex();
    gl_Position = uProjectionMatrix * vec4(p12, 1, 1);
    gOut.color = color1;
    EmitVertex();
    gl_Position = uProjectionMatrix * vec4(p22, 1, 1);
    gOut.color = color2;
    EmitVertex();
    EndPrimitive();
    gl_Position = uProjectionMatrix * vec4(p22, 1, 1);
    gOut.color = color2;
    EmitVertex();
    gl_Position = uProjectionMatrix * vec4(p21, 1, 1);
    gOut.color = color2;
    EmitVertex();
    gl_Position = uProjectionMatrix * vec4(p11, 1, 1);
    gOut.color = color1;
    EmitVertex();
    EndPrimitive();
}

void main() {
    float inc = 1.0 / vOut[0].segments;
    float maxV = 1.0 - inc;
    for (float d = 0; d < maxV; d += inc) {
        drawSegment(d, d + inc, vOut[0].pos1, vOut[0].control1, vOut[0].pos2, vOut[0].control2, mix(vOut[0].color1, vOut[0].color2, d), mix(vOut[0].color1, vOut[0].color2, d + inc), vOut[0].width);
    }
}
