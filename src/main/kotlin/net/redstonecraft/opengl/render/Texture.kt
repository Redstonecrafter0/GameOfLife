package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.interfaces.Pointed
import org.lwjgl.BufferUtils
import org.lwjgl.nanovg.NanoSVG.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12.*
import org.lwjgl.system.MemoryUtil.NULL
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer

open class Texture(data: ByteBuffer?, val width: Int, val height: Int, val format: Int = GL_RGBA) : Pointed {

    constructor(img: BufferedImage, width: Int, height: Int) : this(img.toRawData(), width, height)

    override val pointer = glGenTextures()

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, pointer)
    }

    fun update(data: ByteBuffer) {
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data)
    }

    fun updatePartial(data: ByteBuffer, xOff: Int, yOff: Int, w: Int, h: Int) {
        glTexSubImage2D(GL_TEXTURE_2D, 0, xOff, yOff, w, h, format, GL_UNSIGNED_BYTE, data)
    }

    init {
        bind()
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        if (data != null) {
            update(data)
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, NULL)
        }
    }

}

class SVGTexture(code: String, width: Int, height: Int) : Texture(code.toSvg(width, height), width, height) {

    constructor(file: File, width: Int, height: Int) : this(file.readText(), width, height)

}

private fun BufferedImage.toRawData(): ByteBuffer {
    val pixels = IntArray(height * width)
    getRGB(0, 0, width, height, pixels, 0, width)
    val buffer = BufferUtils.createByteBuffer(width * height * 4)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = pixels[y * width + x]
            buffer.put((pixel shr 16 and 0xFF).toByte())
            buffer.put((pixel shr 8 and 0xFF).toByte())
            buffer.put((pixel shr 0 and 0xFF).toByte())
            buffer.put((pixel shr 24 and 0xFF).toByte())
        }
    }
    buffer.flip()
    return buffer
}

private fun String.toSvg(width: Int, height: Int): ByteBuffer {
    val rast = nsvgCreateRasterizer()
    val svg = nsvgParse(this, "px", 96F)!!
    val image = ByteBuffer.allocate(width * height * 4)
    nsvgRasterize(rast, svg, 0F, 0F, 1F, image, width, height, width * 4)
    nsvgDeleteRasterizer(rast)
    return image
}
