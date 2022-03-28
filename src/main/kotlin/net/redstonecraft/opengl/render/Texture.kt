package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.interfaces.Pointed
import org.lwjgl.nanovg.NanoSVG.*
import org.lwjgl.opengl.GL12.*
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer

open class Texture(data: ByteBuffer, val width: Int, val height: Int, val format: Int = GL_RGBA) : Pointed {

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
        update(data)
    }

}

class SVGTexture(code: String, width: Int, height: Int) : Texture(code.toSvg(width, height), width, height) {

    constructor(file: File, width: Int, height: Int) : this(file.readText(), width, height)

}

private fun BufferedImage.toRawData(): ByteBuffer {
    val data = ByteArray(height * width * 4)
    for (y in 0..height) {
        for (x in 0..width) {
            data[y * width + x] = getRGB(x, y).toByte()
        }
    }
    return ByteBuffer.wrap(data)
}

private fun String.toSvg(width: Int, height: Int): ByteBuffer {
    val rast = nsvgCreateRasterizer()
    val svg = nsvgParse(this, "px", 96F)!!
    val image = ByteBuffer.allocate(width * height * 4)
    nsvgRasterize(rast, svg, 0F, 0F, 1F, image, width, height, width * 4)
    nsvgDeleteRasterizer(rast)
    return image
}
