#version 330 core

layout (location=0) in vec2 aPos1;
layout (location=1) in vec2 aPos2;
layout (location=2) in vec2 aControl1;
layout (location=3) in vec2 aControl2;

out BezierData {
    vec2 pos1;
    vec2 pos2;
    vec2 control1;
    vec2 control2;
} vOut;

void main() {
    vOut.pos1 = aPos1;
    vOut.pos2 = aPos2;
    vOut.control1 = aControl1;
    vOut.control2 = aControl2;
}
