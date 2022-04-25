package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import net.redstonecraft.opengl.copy
import org.joml.Vector2f
import org.lwjgl.opengl.GL15.*
import kotlin.math.exp

class HorizontalBlurRenderer(
    camera: Camera = OrthographicCamera(0F, 1920F, 0F, 1080F),
    sigma: Float = 1.9F
) {

    val batch = BlurBatch(ShaderProgram(
        VertexShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/blur/hori_vert.glsl")!!.readBytes()
                .decodeToString()
        ),
        FragmentShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/blur/frag.glsl")!!.readBytes()
                .decodeToString()
        )
    ), camera, false)

    var texture: Texture? = null
        set(value) {
            field = value
            batch.texture = value!!
        }

    var mask: Texture? = null
        set(value) {
            field = value
            batch.mask = value!!
        }

    var sigma: Float = sigma
        set(value) {
            field = value
            batch.weights = generateGaussianWeights(value)
        }

    fun render(pos: Vector2f, size: Vector2f, texPos: Vector2f = Vector2f(0F, 0F), texSize: Vector2f = Vector2f(1F, 1F)) {
        batch.texture(pos, size, texPos, texSize)
    }

    fun finish() = batch.flush()

    init {
        batch.weights = generateGaussianWeights(sigma)
    }

}

class VerticalBlurRenderer(
    camera: Camera = OrthographicCamera(0F, 1920F, 0F, 1080F),
    sigma: Float = 1.9F
) {

    val batch = BlurBatch(ShaderProgram(
        VertexShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/blur/vert_vert.glsl")!!.readBytes()
                .decodeToString()
        ),
        FragmentShader(
            TextureRenderer::class.java.getResourceAsStream("/assets/shader/blur/frag.glsl")!!.readBytes()
                .decodeToString()
        )
    ), camera, true)

    var texture: Texture? = null
        set(value) {
            field = value
            batch.texture = value!!
        }

    var mask: Texture? = null
        set(value) {
            field = value
            batch.mask = value!!
        }

    var sigma: Float = sigma
        set(value) {
            field = value
            batch.weights = generateGaussianWeights(value)
        }

    fun render(pos: Vector2f, size: Vector2f, texPos: Vector2f = Vector2f(0F, 0F), texSize: Vector2f = Vector2f(1F, 1F)) {
        batch.texture(pos, size, texPos, texSize)
    }

    fun finish() = batch.flush()

    init {
        batch.weights = generateGaussianWeights(sigma)
    }

}

class BlurBatch(
    shader: ShaderProgram,
    val camera: Camera,
    val vertical: Boolean
) : Batch(
    256,
    shader,
    2, 2
) {

    val vertices = FloatArray(size * vertSize) { 0F }
    lateinit var texture: Texture
    lateinit var mask: Texture

    lateinit var weights: FloatArray

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

    fun texture(pos: Vector2f, size: Vector2f, texPos: Vector2f, texSize: Vector2f) {
        val pos2 = pos.copy.add(size)
        val texPos2 = texPos.copy.add(texSize)
        if (count >= this.size - 3) flush()
        vert(
            Vector2f(pos2.x, pos.y),
            Vector2f(texPos2.x, texPos.y)
        )
        vert(
            pos2,
            texPos2
        )
        vert(
            Vector2f(pos.x, pos2.y),
            Vector2f(texPos.x, texPos2.y)
        )
        vert(
            pos,
            texPos
        )
    }

    private fun vert(pos: Vector2f, texCoords: Vector2f) {
        vertices[count * vertSize + 0] = pos.x
        vertices[count * vertSize + 1] = pos.y

        vertices[count * vertSize + 2] = texCoords.x
        vertices[count * vertSize + 3] = texCoords.y
        count++
    }

    override fun upload(shader: ShaderProgram) {
        shader.uploadUMat4f("uProjectionMatrix", camera.projectionMatrix)
        shader.uploadUFloat("uSize", 1F / (if (vertical) texture.height else texture.width))
        shader.uploadUTexture("uTexture", texture, 0)
        shader.uploadUTexture("uMask", mask, 1)
        shader.uploadUFloatArray("uWeights", weights)
    }

    override fun bufferData() {
        bufferVbo((Float.SIZE_BYTES * vertSize * size).toLong(), vertices, GL_DYNAMIC_DRAW)
    }

}

fun generateGaussianWeights(sigma: Float, size: Int = 7): FloatArray {
    val weights = FloatArray(size)
    val twoSigmaSquare = 2 * sigma * sigma
    var sum = 0F
    for (i in 0 until size) {
        val x = i.toFloat() - (size - 1) / 2
        val g = (1 / (twoSigmaSquare * Math.PI)).toFloat() * exp(-x * x / twoSigmaSquare)
        weights[i] = g
        sum += g
    }
    for (i in 0 until size) {
        weights[i] /= sum
    }
    return weights
}
