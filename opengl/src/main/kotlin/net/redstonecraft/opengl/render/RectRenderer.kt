package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import net.redstonecraft.opengl.copy
import org.joml.Vector2f
import org.lwjgl.opengl.GL15.*
import java.awt.Color

class RectRenderer(
    camera: Camera = OrthographicCamera(0F, 1920F, 0F, 1080F)
) {

    val batch = RectBatch(camera)

    fun render(pos: Vector2f, size: Vector2f, color1: Color = Color.WHITE, color2: Color = color1, color3: Color = color1, color4: Color = color1) {
        batch.render(pos, size, color1, color2, color3, color4)
    }

    fun finish() = batch.flush()

}

class RectBatch(
    val camera: Camera
) : Batch(
    1024,
    ShaderProgram(
        VertexShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/rect/vert.glsl")!!.readBytes()
                .decodeToString()
        ),
        FragmentShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/rect/frag.glsl")!!.readBytes()
                .decodeToString()
        )
    ),
    GL_TRIANGLES,
    2, 4
) {

    val vertices = FloatArray(size * vertSize) { 0F }

    companion object {
        private val indices = intArrayOf(0, 1, 3, 1, 2, 3)
    }

    override fun postEbo(id: Int) {
        val elementBuffer = IntArray(size * 3)
        for (i in elementBuffer.indices) {
            elementBuffer[i] = indices[i % 6] + i / 6 * 4
        }
        bufferEbo(0, elementBuffer, GL_STATIC_DRAW, false)
    }

    fun render(pos: Vector2f, size: Vector2f, color1: Color, color2: Color, color3: Color, color4: Color) {
        val pos2 = pos.copy.add(size)
        if (count >= this.size - 3) flush()
        vert(Vector2f(pos2.x, pos.y), color1)
        vert(pos2, color2)
        vert(Vector2f(pos.x, pos2.y), color3)
        vert(pos, color4)
    }

    private fun vert(pos: Vector2f, color: Color) {
        vertices[count * vertSize + 0] = pos.x
        vertices[count * vertSize + 1] = pos.y

        vertices[count * vertSize + 2] = color.red / 255F
        vertices[count * vertSize + 3] = color.green / 255F
        vertices[count * vertSize + 4] = color.blue / 255F
        vertices[count * vertSize + 5] = color.alpha / 255F
        count++
    }

    override fun ShaderProgram.upload() {
        uploadUMat4f("uProjectionMatrix", camera.projectionMatrix)
    }

    override fun bufferData() {
        bufferVbo((Float.SIZE_BYTES * vertSize * size).toLong(), vertices, GL_DYNAMIC_DRAW)
    }

}
