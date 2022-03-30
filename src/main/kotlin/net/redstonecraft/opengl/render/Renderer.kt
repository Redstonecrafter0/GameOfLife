package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.copy
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL15.*
import java.io.Closeable

abstract class Renderer(val shader: ShaderProgram): Closeable {

    val vbo = glGenBuffers()
    val vao = glGenBuffers()
    val ebo = glGenBuffers()

    fun render(block: Builder.() -> Unit) {
        val builder = Builder()
        builder.block()
    }

    abstract fun uploadUniformShader(shader: ShaderProgram)

    override fun close() {
    }

    inner class Builder {
        val vertices = mutableListOf<Vector3f>()
        val indices = mutableListOf<Int>()
        val color = mutableListOf<Vector4f>()
        val colorIndices = mutableListOf<Int>()
        val texture = mutableListOf<Vector2f>()
        val textureIndices = mutableListOf<Int>()

        val resultVertices: FloatArray
            get() = (
                    vertices.map { listOf(it.x, it.y, it.z) }.flatten() +
                            color.map { listOf(it.x, it.y, it.z, it.w) }.flatten() +
                            texture.map { listOf(it.x, it.y) }.flatten()
                    ).toFloatArray()

//        val resultIndices: IntArray
//            get() = (
//                    indices.map { listOf(it.x, it.y, it.z) }.flatten() +
//                            colorIndices.map { listOf(it.x, it.y, it.z, it.w) }.flatten() +
//                            textureIndices.map { listOf(it.x, it.y) }.flatten()
//                    )

        fun triangle(p1: Vector3f, p2: Vector3f, p3: Vector3f) {
            if (p1 !in vertices) vertices += p1.copy
            if (p2 !in vertices) vertices += p2.copy
            if (p3 !in vertices) vertices += p3.copy
            indices += vertices.indexOf(p1)
            indices += vertices.indexOf(p2)
            indices += vertices.indexOf(p3)
        }

        fun triangleColor(p1: Vector3f, p2: Vector3f, p3: Vector3f, c: Vector4f) {
            triangle(p1, p2, p3)
            if (c !in color) color += c
            colorIndices += color.indexOf(c)
        }

        fun triangleTexture(p1: Vector3f, p2: Vector3f, p3: Vector3f, t: Vector2f) {
            triangle(p1, p2, p3)
            if (t !in texture) texture += t
            textureIndices += texture.indexOf(t)
        }
    }
}
