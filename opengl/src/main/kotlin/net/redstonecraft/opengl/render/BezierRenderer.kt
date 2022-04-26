package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import org.joml.Vector2f
import org.lwjgl.opengl.GL15.*
import java.awt.Color

class BezierRenderer(
    camera: Camera = OrthographicCamera(0F, 1920F, 0F, 1080F)
) {

    val batch = BezierBatch(camera)

    fun cubic(pos1: Vector2f, c1: Vector2f, c2: Vector2f, pos2: Vector2f, width: Float, color: Color, color2: Color = color, segments: Int) {
        batch.render(pos1, c1, c2, pos2, width, color, color2, segments)
    }

    fun quadratic(pos1: Vector2f, c: Vector2f, pos2: Vector2f, width: Float, color: Color, color2: Color = color, segments: Int) {
        batch.render(pos1, c, c, pos2, width, color, color2, segments)
    }

    fun linear(pos1: Vector2f, pos2: Vector2f, width: Float, color: Color, color2: Color = color, segments: Int = 1) {
        batch.render(pos1, pos1, pos2, pos2, width, color, color2, segments)
    }

    fun finish() = batch.flush()

}

class BezierBatch(
    val camera: Camera
) : Batch(
    512,
    ShaderProgram(
        VertexShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/bezier/vert.glsl")!!.readBytes()
                .decodeToString()
        ),
        FragmentShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/bezier/frag.glsl")!!.readBytes()
                .decodeToString()
        ),
        GeometryShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/bezier/geom.glsl")!!.readBytes()
                .decodeToString()
        )
    ),
    GL_POINTS,
    2, 2, 2, 2, 4, 4, 1, 1
) {

    val vertices = FloatArray(size * vertSize) { 0F }

    override fun postEbo(id: Int) {
        bufferEbo(0, IntArray(size) { it }, GL_STATIC_DRAW, false)
    }

    fun render(pos1: Vector2f, c1: Vector2f, c2: Vector2f, pos2: Vector2f, width: Float, color: Color, color2: Color, segments: Int) {
        if (count >= this.size) flush()
        vert(pos1, c1, c2, pos2, width, color, color2, segments)
    }

    private fun vert(pos1: Vector2f, c1: Vector2f, c2: Vector2f, pos2: Vector2f, width: Float, color: Color, color2: Color, segments: Int) {
        vertices[count * vertSize + 0] = pos1.x
        vertices[count * vertSize + 1] = pos1.y

        vertices[count * vertSize + 2] = pos2.x
        vertices[count * vertSize + 3] = pos2.y

        vertices[count * vertSize + 4] = c1.x
        vertices[count * vertSize + 5] = c1.y

        vertices[count * vertSize + 6] = c2.x
        vertices[count * vertSize + 7] = c2.y

        vertices[count * vertSize + 8] = color.red / 255F
        vertices[count * vertSize + 9] = color.green / 255F
        vertices[count * vertSize + 10] = color.blue / 255F
        vertices[count * vertSize + 11] = color.alpha / 255F

        vertices[count * vertSize + 12] = color2.red / 255F
        vertices[count * vertSize + 13] = color2.green / 255F
        vertices[count * vertSize + 14] = color2.blue / 255F
        vertices[count * vertSize + 15] = color2.alpha / 255F

        vertices[count * vertSize + 16] = width
        vertices[count * vertSize + 17] = segments.toFloat()
        count++
    }

    override fun ShaderProgram.upload() {
        uploadUMat4f("uProjectionMatrix", camera.projectionMatrix)
    }

    override fun bufferData() {
        bufferVbo((Float.SIZE_BYTES * vertSize * size).toLong(), vertices, GL_DYNAMIC_DRAW)
    }

}
