package net.redstonecraft.opengl.camera

import org.joml.Matrix4f

interface Camera {
    val projectionMatrix: Matrix4f
}

class OrthographicCamera(left: Float, right: Float, bottom: Float, top: Float) : Camera {
    override val projectionMatrix: Matrix4f = Matrix4f().ortho(left, right, bottom, top, 0F, 100F)
}

class PerspectiveCamera(val fov: Float, val aspect: Float, val near: Float, val far: Float) : Camera {

    override val projectionMatrix: Matrix4f = Matrix4f().perspective(fov, aspect, near, far)
}
