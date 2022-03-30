package net.redstonecraft.opengl.render

import com.mlomb.freetypejni.FreeType
import com.mlomb.freetypejni.FreeTypeConstants.FT_LOAD_RENDER
import kotlinx.coroutines.*
import net.redstonecraft.opengl.camera.Camera
import org.lwjgl.opengl.GL11.GL_RED
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO
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

//    constructor(data: ByteArray, textureWidth: Int) : this(data, textureWidth, chars)

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

        private val freetype = FreeType.newLibrary()!!

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

        fun generateFont(font: Font, chars: List<Char>, size: Int, spacing: Int, output: File): SDFFont {
            val font = font.deriveFont(size.toFloat())
            val rImg = BufferedImage(4096, 4096, BufferedImage.TYPE_INT_ARGB)
            val gImg = BufferedImage(4096, 4096, BufferedImage.TYPE_INT_ARGB)
            val bImg = BufferedImage(4096, 4096, BufferedImage.TYPE_INT_ARGB)
            val aImg = BufferedImage(4096, 4096, BufferedImage.TYPE_INT_ARGB)
            val charsMap = mutableMapOf<Int, Glyph>()
            for (char in chars) {
                val cImg = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
                val g2d = cImg.createGraphics()
                g2d.font = font
                charsMap[char.code] = Glyph(char.code, x, y, height, width, bearingX, bearingY, g2d.fontMetrics.charWidth(char))
            }
            val fBuffer = ByteArray(4096 * 4096 * 4) { 0 }
            for (x in 0..4096) {
                for (y in 0..4096) {
                    fBuffer[(y * 4096 + x) * 4] = (rImg.getRGB(x, y) and 0xFF).toByte()
                    fBuffer[(y * 4096 + x) * 4 + 1] = (gImg.getRGB(x, y) and 0xFF).toByte()
                    fBuffer[(y * 4096 + x) * 4 + 2] = (bImg.getRGB(x, y) and 0xFF).toByte()
                    fBuffer[(y * 4096 + x) * 4 + 3] = (aImg.getRGB(x, y) and 0xFF).toByte()
                }
            }
            return SDFFont(fBuffer, 4096, charsMap)
        }

        fun generateFont(font: File, chars: List<Char>, size: Int, spacing: Int, output: File): ByteArray {
            val face = freetype.newFace(font.readBytes(), 0)
            val fBitmap = ByteArray(4096 * 4096) { 0 }
            val a: Font = null!!
            Toolkit.getDefaultToolkit().getFontMetrics()
            var channel = 0
            for ((index, char) in chars.withIndex()) {
                println(char)
                FreeType.FT_Set_Pixel_Sizes(face.pointer, 0F, (size * 16).toFloat())
                FreeType.FT_Set_Char_Size(face.pointer, size, size, size, size)
                if (FreeType.FT_Load_Char(face.pointer, char, FT_LOAD_RENDER).also { println(it) }) {
                    val gWidth = face.glyphSlot.bitmap.width
                    val gHeight = face.glyphSlot.bitmap.rows
                    val gBitmap = ByteArray(gWidth * gHeight)
                    face.glyphSlot.bitmap.buffer.get(gBitmap)
                    val widthScale = gWidth.toFloat() / (size * 16).toFloat()
                    val heightScale = gHeight.toFloat() / (size * 16).toFloat()
                    val characterWidth = (size.toFloat() * widthScale).toInt()
                    val characterHeight = (size.toFloat() * heightScale).toInt()
                    val bitmapWidth: Int = characterWidth + spacing * 2
                    val bitmapScaleX = gWidth.toFloat() / characterWidth.toFloat()
                    val bitmapScaleY = gHeight.toFloat() / characterHeight.toFloat()
                    println(spacing)
                    println(characterHeight)
                    println(characterWidth)
                    println("===============")
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
                            ) * 255.0f).toInt().toByte().also { println(it) }
                        }
                    }
                }
            }
            val img = BufferedImage(4096, 4096, BufferedImage.TYPE_BYTE_GRAY)
            val imgData = (img.raster.dataBuffer as DataBufferByte).data
            System.arraycopy(fBitmap, 0, imgData, 0, fBitmap.size)
            ImageIO.write(img, "png", File("out.png"))
            return fBitmap
        }
    }
}

fun main() {
    SDFFont.generateFont(Font.createFont(Font.TRUETYPE_FONT, File("JetBrainsMonoNL-Regular.ttf")), "abcdefgABCDEFG".toList(), 64, 32, File("out.font"))
}
