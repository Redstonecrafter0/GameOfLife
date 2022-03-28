package net.redstonecraft.opengl.render

import com.mlomb.freetypejni.FreeType
import com.mlomb.freetypejni.FreeTypeConstants.FT_LOAD_RENDER
import kotlinx.coroutines.*
import net.redstonecraft.opengl.camera.Camera
import org.lwjgl.opengl.GL11.GL_RED
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.sqrt

class SDFFontRenderer(val camera: Camera, val font: SDFFont) : Renderer(
    ShaderProgram(
        VertexShader(
            SDFFontRenderer::class.java.getResourceAsStream("/assets/shader/sdf/vert.glsl")!!.readBytes()
                .contentToString()
        ), FragmentShader(
            SDFFontRenderer::class.java.getResourceAsStream("/assets/shader/sdf/frag.glsl")!!.readBytes()
                .contentToString()
        )
    )
) {

    val texture = Texture(ByteBuffer.wrap(font.texture), font.textureWidth, font.textureHeight, GL_RED)

    override fun uploadUniformShader(shader: ShaderProgram) {
        shader.uploadUniformMat4f("uProjectionMatrix", camera.projectionMatrix)
        shader.uploadTexture("uTexture", texture)
    }

    fun render(text: String, x: Int, y: Int, size: Int) {
        texture.bind()
    }

}

class SDFFont(val texture: ByteArray, val textureWidth: Int, val chars: Map<Int, Glyph>) {

    constructor(data: ByteArray, textureWidth: Int) : this(data, textureWidth, chars)

    val textureHeight by lazy { texture.size / textureWidth }

    /*
    * META
    *   LENGTH INT32
    *   ARRAY
    *     CODE INT32
    *     X INT32
    *     Y INT32
    *     HEIGHT INT32
    *     WIDTH INT32
    *     BEARINGX INT32
    *     BEARINGY INT32
    *     ADVANCE INT32
    * IMG WIDTH INT32
    * IMG GREYSCALE BYTEARRAY
    * */

    data class Glyph(val code: Int, val x: Int, val y: Int, val height: Int, val width: Int, val bearingX: Int,
                     val bearingY: Int, val advance: Int)

    companion object {

        val freetype = FreeType.newLibrary()

        val FONT_SIZE = 64
        val FONT_SPACING = 32

        private fun mapRange(value: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float): Float {
            return (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
        }

        private fun getPixel(x: Int, y: Int, bitmap: ByteArray, width: Int, height: Int): Boolean {
            return x in 0 until width && y in 0 until height && bitmap[x + y * width].toInt() and 0xFF != 0
        }

        private fun findNearestPixel(pixelX: Int, pixelY: Int, bitmap: ByteArray, width: Int, height: Int,
                                     spread: Int): Float {
            val state: Boolean = getPixel(pixelX, pixelY, bitmap, width, height)
            val minX = pixelX - spread
            val maxX = pixelX + spread
            val minY = pixelY - spread
            val maxY = pixelY + spread
            val minDistance = sqrt((minY until maxY).map { it to (minX until maxX).toList() }.map {
                it.second.map { x ->
                    val y = it.first
                    val pixelState = getPixel(x, y, bitmap, width, height)
                    val dxSquared = ((x - pixelX) * (x - pixelX)).toFloat()
                    val dySquared = ((y - pixelY) * (y - pixelY)).toFloat()
                    if (pixelState) dxSquared + dySquared else (spread * spread).toFloat()
                }
            }.flatten().minOrNull() ?: (spread * spread).toFloat())
            var output = (minDistance - 0.5f) / (spread - 0.5f)
            output *= if (!state) -1F else 1.toFloat()
            return (output + 1) * 0.5f
        }

        fun generateFont(font: File, chars: List<Int>, size: Int, spacing: Int, output: File): ByteArray {
            val face = freetype.newFace(font.readBytes(), 0)
            val fBitmap = ByteArray(4096 * 4096) { 0 }
            for ((index, char) in chars.withIndex()) {
                FreeType.FT_Set_Pixel_Sizes(face.pointer, 0F, (size * 16).toFloat())
                if (FreeType.FT_Load_Char(face.pointer, char.toChar(), FT_LOAD_RENDER)) {
                    val gWidth = face.glyphSlot.bitmap.width
                    val gHeight = face.glyphSlot.bitmap.rows
                    val gBitmap = ByteArray(gWidth * gHeight)
                    face.glyphSlot.bitmap.buffer.get(gBitmap)
                    val widthScale = gWidth.toFloat() / (size * 16).toFloat()
                    val heightScale = gHeight.toFloat() / (size * 16).toFloat()
                    val characterWidth = (size.toFloat() * widthScale).toInt()
                    val characterHeight = (size.toFloat() * heightScale).toInt()
                    val bitmapWidth: Int = characterWidth + spacing * 2
                    val bitmapHeight: Int = characterHeight + spacing * 2
                    val bitmapScaleX = gWidth.toFloat() / characterWidth.toFloat()
                    val bitmapScaleY = gHeight.toFloat() / characterHeight.toFloat()
                    for (y in -spacing until characterHeight + spacing) {
                        for (x in -spacing until characterWidth + spacing) {
                            val pixelX = mapRange(
                                x.toFloat(), -spacing.toFloat(), (characterWidth + spacing).toFloat(),
                                -spacing * bitmapScaleX, (characterWidth + spacing) * bitmapScaleX
                            ).toInt()
                            val pixelY = mapRange(
                                y.toFloat(), -spacing.toFloat(), (characterHeight + spacing).toFloat(),
                                -spacing * bitmapScaleY, (characterHeight + spacing) * bitmapScaleY
                            ).toInt()
                            fBitmap[x + spacing + (y + spacing) * bitmapWidth] = (findNearestPixel(
                                pixelX, pixelY, gBitmap, gWidth, gHeight, size * 8
                            ) * 255.0f).toInt().toByte()
                        }
                    }
                }
            }
        }
    }
}
