package net.redstonecraft.opengl.buffer

import net.redstonecraft.opengl.interfaces.Pointed
import net.redstonecraft.opengl.render.*
import org.lwjgl.opengl.GL30.*
import java.awt.Color
import java.io.Closeable
import kotlin.properties.Delegates

open class Framebuffer(width: Int, height: Int) : Pointed, Closeable {

    var width: Int = width
        private set

    var height: Int = height
        private set

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

        fun unbind() {
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
        }

        fun clear(color: Color) {
            glClearColor(color.red / 255F, color.green / 255F, color.blue / 255F, color.alpha / 255F)
            glClear(GL_COLOR_BUFFER_BIT)
        }

        fun clearColorDepthStencil(color: Color) {
            glClearColor(color.red / 255F, color.green / 255F, color.blue / 255F, color.alpha / 255F)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        }
    }

    final override var pointer by Delegates.notNull<Int>()
        private set

    lateinit var texture: Texture
        private set

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, pointer)
    }

    open fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        close()
        init()
        postInit()
    }

    init {
        init()
        postInit()
    }

    open fun init() {
        pointer = glGenFramebuffers()
        bind()
        texture = Texture(width, height, GL_RGBA)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

    fun postInit() {
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("Framebuffer $pointer not complete.")
        }
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
}

class Framebuffer2Texture(width: Int, height: Int) : Framebuffer(width, height) {

    var texture2 = Texture(width, height, GL_RED)
        private set

    override fun init() {
        super.init()
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texture2.pointer, 0)
    }

    override fun blit(blitTex: Texture) {
        throw NotImplementedError("Can't blit on a framebuffer with more than one color attachment.")
    }

}

class FramebufferDepthStencil(width: Int, height: Int) : Framebuffer(width, height) {

    private var pRbo: Int? = null

    var rbo: Int
        get() = pRbo!!
        private set(value) {
            pRbo = value
        }

    override fun init() {
        super.init()
        rbo = glGenRenderbuffers()
        glBindRenderbuffer(pointer, rbo)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo)
    }

    override fun close() {
        super.close()
        glDeleteRenderbuffers(rbo)
    }

}
