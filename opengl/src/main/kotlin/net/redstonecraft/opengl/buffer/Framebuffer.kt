package net.redstonecraft.opengl.buffer

import net.redstonecraft.opengl.interfaces.Pointed
import net.redstonecraft.opengl.render.Texture
import org.lwjgl.opengl.GL30.*

class Framebuffer(width: Int, height: Int) : Pointed {

    override var pointer = glGenFramebuffers()
        private set

    var texture = Texture(null, width, height, GL_RGBA)
        private set

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, pointer)
    }

    fun resize(width: Int, height: Int) {
        glDeleteFramebuffers(pointer)
        pointer = glGenFramebuffers()
        bind()
        texture = Texture(null, width, height, GL_RGBA)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

    init {
        bind()
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

}
