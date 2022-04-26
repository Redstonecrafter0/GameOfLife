package net.redstonecraft.opengl.buffer

import net.redstonecraft.opengl.interfaces.Pointed
import net.redstonecraft.opengl.render.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO

open class Framebuffer(var width: Int, var height: Int) : Pointed, Closeable {

    companion object {
        private var blitShaderInternal: ShaderProgram? = null
        private val blitShader: ShaderProgram
            get() {
                if (blitShaderInternal == null) {
                    blitShaderInternal = ShaderProgram(
                        VertexShader(
                            Framebuffer::class.java.getResourceAsStream("/assets/blit/vert_vert.glsl")!!.readBytes().decodeToString()
                        ),
                        FragmentShader(
                            Framebuffer::class.java.getResourceAsStream("/assets/blit/frag.glsl")!!.readBytes().decodeToString()
                        )
                    )
                }
                return blitShaderInternal!!
            }
        private var blitBatchInternal: BlitBatch? = null
        private val blitBatch: BlitBatch
            get() {
                if (blitBatchInternal == null) {
                    blitBatchInternal = BlitBatch()
                }
                return blitBatchInternal!!
            }

        class BlitBatch: Batch(1, blitShader, GL_TRIANGLES, 2, 2) {

            override fun postEbo(id: Int) {
                bufferEbo(0, intArrayOf(0, 1, 3, 1, 2, 3), GL_STATIC_DRAW, false)
            }

            override fun postVbo(id: Int) {
                bufferVbo(0, floatArrayOf(1F, 1F, 1F, 1F, 1F, -1F, 1F, 0F, -1F, -1F, 0F, 0F, -1F, 1F, 0F, 1F), GL_STATIC_DRAW, false)
            }

            lateinit var currentTexture: Texture

            override fun ShaderProgram.upload() {
                uploadUTexture("uTexture", currentTexture)
            }

            override fun bufferData() {}
        }
    }

    final override var pointer = glGenFramebuffers()
        private set

    var texture = Texture(width, height, GL_RGBA)
        private set

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, pointer)
    }

    fun resize(width: Int, height: Int) {
        close()
        pointer = glGenFramebuffers()
        bind()
        texture = Texture(width, height, GL_RGBA)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

    init {
        bind()
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

    open fun blit(blitTex: Texture) {
        blitBatch.count++
        blitBatch.currentTexture = blitTex
        blitBatch.flush()
    }

    override fun close() {
        glDeleteFramebuffers(pointer)
        texture.close()
    }

    // convert a bytebuffer to a byte array
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    private fun ByteArray.toIntArray(): IntArray {
        val data = IntArray(size / 4)
        for (i in data.indices) {
            data[i] = (this[i * 4 + 3].toInt() shl 24) or (this[i * 4 + 2].toInt() shl 16) or (this[i * 4 + 1].toInt() shl 8) or this[i * 4].toInt()
        }
        return data
    }

}

class Framebuffer2(width: Int, height: Int) : Framebuffer(width, height) {

    var texture2 = Texture(width, height, GL_RED)
        private set

    init {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texture2.pointer, 0)
    }

    override fun blit(blitTex: Texture) {
        throw NotImplementedError("Can't blit on a framebuffer with more than one color attachment.")
    }

}
