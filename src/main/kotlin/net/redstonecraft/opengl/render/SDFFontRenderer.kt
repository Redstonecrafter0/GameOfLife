package net.redstonecraft.opengl.render

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import org.joml.Matrix4f
import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import javax.imageio.ImageIO

class SDFFontRenderer(
    font: SDFFont,
    fontSize: Float = 12F,
    camera: Camera = OrthographicCamera(0F, 1920F, 1080F, 0F),
    batchSize: Int = 5000
) {

    val batch = SDFFontBatch(font, fontSize, camera, batchSize)

    val texture = font.texture

    fun render(text: String, x: Float, y: Float) {
    }

    fun finish() = batch.flush()

}

class SDFFontBatch(
    val font: SDFFont,
    val fontSize: Float,
    val camera: Camera = OrthographicCamera(0F, 1920F, 1080F, 0F),
    size: Int = 5000) : Batch(
    size,
    ShaderProgram(
        VertexShader(
            SDFFontRenderer::class.java.getResourceAsStream("/assets/shader/msdf/vert.glsl")!!.readBytes()
                .contentToString()
        ), FragmentShader(
            SDFFontRenderer::class.java.getResourceAsStream("/assets/shader/msdf/frag.glsl")!!.readBytes()
                .contentToString()
        )
    ),
    4, 4, 4, 2
) {

    val vertices = FloatArray(size * vertSize) { 0F }

    companion object {
        private val indices = intArrayOf(0, 1, 3, 1, 2, 3)
        private val projectionMatrix = Matrix4f().ortho(0F, 1920F, 1080F, 0F, 0F, 100F)
    }

    override fun postEbo(id: Int) {
        val elementBuffer = IntArray(size * 3)
        for (i in elementBuffer.indices) {
            elementBuffer[i] = indices[i % 6] + i / 6 * 4
        }
        bufferEbo(0, elementBuffer, GL_STATIC_DRAW, false)
    }

    fun bufferChar(code: Byte, x: Float, y: Float) {
        vertices
    }

    override fun upload(shader: ShaderProgram) {
        shader.uploadUniformMat4f("uProjectionMatrix", camera.projectionMatrix)
        shader.uploadTexture("uTexture", font.texture)
        shader.uploadUniformFloat("uScreenPxRange", font.getScreenPxDistance(fontSize))
    }

    override fun bufferData() {
        bufferVbo((Float.SIZE_BYTES * vertSize * size).toLong(), vertices, GL_DYNAMIC_DRAW)
    }
}

class SDFFont(texture: ByteArray, jsonAtlas: String) {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    val defs = json.decodeFromString<Data>(jsonAtlas)

    val glyphs = defs.glyphs.associateBy { it.unicode }

    fun getScreenPxDistance(size: Float) = (size / defs.atlas.size * defs.atlas.distanceRange).toFloat()

    val texture = Texture(ImageIO.read(texture.inputStream()), defs.atlas.width, defs.atlas.height)

    data class Data(val atlas: Atlas, val metrics: Metrics, val glyphs: List<Glyph>)

    data class Atlas(val type: String, val distanceRange: Double, val size: Double, val width: Int, val height: Int,
                     val yOrigin: String)

    data class Metrics(val emSize: Double, val lineHeight: Double, val ascender: Double, val descender: Double,
                       val underlineY: Double, val underlineThickness: Double)

    data class Glyph(val unicode: Byte, val planeBounds: Bounds = Bounds(.0, .0, .0, .0),
                     val atlasBounds: Bounds = Bounds(.0, .0, .0, .0))

    data class Bounds(val top: Double, val left: Double, val right: Double, val bottom: Double) {

        val fTop = top.toFloat()
        val fLeft = left.toFloat()
        val fRight = right.toFloat()
        val fBottom = bottom.toFloat()
    }
}
