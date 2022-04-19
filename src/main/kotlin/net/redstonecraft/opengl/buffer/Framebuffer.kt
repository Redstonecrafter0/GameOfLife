package net.redstonecraft.opengl.buffer

import net.redstonecraft.opengl.interfaces.Pointed
import net.redstonecraft.opengl.render.Texture
import org.lwjgl.opengl.GL30.*

class Framebuffer(width: Int, height: Int) : Pointed {

    override val pointer = glGenFramebuffers()

    val texture = Texture(null, width, height, GL_RGBA)

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, pointer)
    }

    init {
        bind()
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.pointer, 0)
    }

}
