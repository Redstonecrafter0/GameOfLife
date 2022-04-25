package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import net.redstonecraft.opengl.copy
import org.joml.Vector2f
import org.lwjgl.opengl.GL15.*
import java.awt.Color

class MaskRenderer(
    camera: Camera = OrthographicCamera(0F, 1920F, 0F, 1080F)
) {

    val batch = MaskBatch(camera)

    var mask by batch::mask
    var flipMaskY by batch::flipMaskY

    fun render(texture: Texture, pos: Vector2f, size: Vector2f = Vector2f(texture.width.toFloat(), texture.height.toFloat()), color: Color = Color.WHITE, texPos: Vector2f = Vector2f(0F, 0F), texSize: Vector2f = Vector2f(1F, 1F)) {
        batch.texture(texture, pos, size, color, texPos, texSize)
    }

    fun finish() = batch.flush()

}

class MaskBatch(
    val camera: Camera
) : Batch(
    15,
    ShaderProgram(
        VertexShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/mask/vert.glsl")!!.readBytes()
                .decodeToString()
        ),
        FragmentShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/mask/frag.glsl")!!.readBytes()
                .decodeToString()
        )
    ),
    2, 2, 4, 1
) {

    val vertices = FloatArray(size * vertSize) { 0F }
    val textures = mutableListOf<Texture>()

    lateinit var mask: Texture

    var flipMaskY = false

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

    fun texture(texture: Texture, pos: Vector2f, size: Vector2f, color: Color, texPos: Vector2f, texSize: Vector2f) {
        val pos2 = pos.copy.add(size)
        val texPos2 = texPos.copy.add(texSize)
        if (count >= this.size - 3) flush()
        vert(
            Vector2f(pos2.x, pos.y),
            Vector2f(texPos2.x, texPos.y),
            color, texture
        )
        vert(
            pos2,
            texPos2,
            color, texture
        )
        vert(
            Vector2f(pos.x, pos2.y),
            Vector2f(texPos.x, texPos2.y),
            color, texture
        )
        vert(
            pos,
            texPos,
            color, texture
        )
    }

    private fun vert(pos: Vector2f, texCoords: Vector2f, color: Color, texture: Texture) {
        vertices[count * vertSize + 0] = pos.x
        vertices[count * vertSize + 1] = pos.y

        vertices[count * vertSize + 2] = texCoords.x
        vertices[count * vertSize + 3] = texCoords.y

        vertices[count * vertSize + 4] = color.red / 255F
        vertices[count * vertSize + 5] = color.green / 255F
        vertices[count * vertSize + 6] = color.blue / 255F
        vertices[count * vertSize + 7] = color.alpha / 255F

        if (texture !in textures) {
            textures += texture
        }
        vertices[count * vertSize + 8] = textures.indexOf(texture).toFloat()
        count++
    }

    override fun upload(shader: ShaderProgram) {
        shader.uploadUMat4f("uProjectionMatrix", camera.projectionMatrix)
        shader.uploadUTextures("uTexture", *textures.toTypedArray())
        shader.uploadUTexture("uMask", mask, textures.size)
        shader.uploadUBoolean("uFlipMaskY", flipMaskY)
    }

    override fun bufferData() {
        bufferVbo((Float.SIZE_BYTES * vertSize * size).toLong(), vertices, GL_DYNAMIC_DRAW)
    }

}
