package net.redstonecraft.opengl.render

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.opengl.camera.Camera
import net.redstonecraft.opengl.camera.OrthographicCamera
import org.joml.Vector2f
import org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import java.awt.Color
import javax.imageio.ImageIO
import kotlin.streams.toList

class SDFFontRenderer(
    font: SDFFont,
    fontSize: Float = 12F,
    camera: Camera = OrthographicCamera(1F, 1F, 1F, 1F),
    batchSize: Int = 5000
) {

    val batch = SDFFontBatch(font, fontSize, camera, batchSize)

    val texture = font.texture

    fun render(text: String, x: Float, y: Float, color: Color = Color.WHITE, bgColor: Color = Color(0, 0, 0, 0)) {
        var yOff = .0
        text.split("\n").map {
            renderLine(it, x, (y + yOff).toFloat(), color, bgColor)
            yOff += (batch.font.defs.metrics.lineHeight * batch.fontSize)
        }
    }

    private fun renderLine(text: String, x: Float, y: Float, color: Color, bgColor: Color) {
        var xOff = .0
        val data = text.chars().toList()
        for (char in data) {
            xOff += batch.bufferChar(char, (x + xOff).toFloat(), y, color, bgColor)
        }
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
                .decodeToString()
        ), FragmentShader(
            SDFFontRenderer::class.java.getResourceAsStream("/assets/shader/msdf/frag.glsl")!!.readBytes()
                .decodeToString()
        )
    ),
    2, 4, 4, 2
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

    fun bufferChar(code: Int, x: Float, y: Float, color: Color, bgColor: Color): Double {
        val glyph = font.glyphs[code] ?: font.glyphs[32] ?: return .5 * fontSize
        if (count >= size - 3) flush()
        vert(
            Vector2f(
                x + glyph.planeBounds.fRight * fontSize,
                y + glyph.planeBounds.fTop * fontSize
            ),
            color, bgColor,
            Vector2f(
                glyph.atlasBounds.fRight / font.defs.atlas.width,
                glyph.atlasBounds.fTop / font.defs.atlas.height
            )
        )
        vert(
            Vector2f(
                x + glyph.planeBounds.fRight * fontSize,
                y + glyph.planeBounds.fBottom * fontSize
            ),
            color, bgColor,
            Vector2f(
                glyph.atlasBounds.fRight / font.defs.atlas.width,
                glyph.atlasBounds.fBottom / font.defs.atlas.height
            )
        )
        vert(
            Vector2f(
                x + glyph.planeBounds.fLeft * fontSize,
                y + glyph.planeBounds.fBottom * fontSize
            ),
            color, bgColor,
            Vector2f(
                glyph.atlasBounds.fLeft / font.defs.atlas.width,
                glyph.atlasBounds.fBottom / font.defs.atlas.height
            )
        )
        vert(
            Vector2f(
                x + glyph.planeBounds.fLeft * fontSize,
                y + glyph.planeBounds.fTop * fontSize
            ),
            color, bgColor,
            Vector2f(
                glyph.atlasBounds.fLeft / font.defs.atlas.width,
                glyph.atlasBounds.fTop / font.defs.atlas.height
            )
        )
        return glyph.advance * fontSize
    }

    private fun vert(pos: Vector2f, color: Color, bgColor: Color, texCoords: Vector2f) {
        vertices[count * vertSize + 0] = pos.x
        vertices[count * vertSize + 1] = pos.y

        vertices[count * vertSize + 2] = color.red / 255F
        vertices[count * vertSize + 3] = color.green / 255F
        vertices[count * vertSize + 4] = color.blue / 255F
        vertices[count * vertSize + 5] = color.alpha / 255F

        vertices[count * vertSize + 6] = bgColor.red / 255F
        vertices[count * vertSize + 7] = bgColor.green / 255F
        vertices[count * vertSize + 8] = bgColor.blue / 255F
        vertices[count * vertSize + 9] = bgColor.alpha / 255F

        vertices[count * vertSize + 10] = texCoords.x
        vertices[count * vertSize + 11] = texCoords.y
        count++
    }

    override fun upload(shader: ShaderProgram) {
        shader.uploadUMat4f("uProjectionMatrix", camera.projectionMatrix)
        shader.uploadUTexture("uTexture", font.texture)
        shader.uploadUFloat("uScreenPxRange", font.getScreenPxDistance(fontSize))
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

    @Serializable
    data class Data(val atlas: Atlas, val metrics: Metrics, val glyphs: List<Glyph>)

    @Serializable
    data class Atlas(val type: String, val distanceRange: Double, val size: Double, val width: Int, val height: Int,
                     val yOrigin: String)

    @Serializable
    data class Metrics(val emSize: Double, val lineHeight: Double, val ascender: Double, val descender: Double,
                       val underlineY: Double, val underlineThickness: Double)

    @Serializable
    data class Glyph(val unicode: Int, val advance: Double, val planeBounds: Bounds = Bounds(.0, .0, .0, .0),
                     val atlasBounds: Bounds = Bounds(.0, .0, .0, .0))

    @Serializable
    data class Bounds(val top: Double, val left: Double, val right: Double, val bottom: Double) {

        val fTop = top.toFloat()
        val fLeft = left.toFloat()
        val fRight = right.toFloat()
        val fBottom = bottom.toFloat()
    }
}
