package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.buffer.Framebuffer
import net.redstonecraft.opengl.render.SDFFont
import net.redstonecraft.opengl.render.SDFFontRenderer
import net.redstonecraft.opengl.render.TextureRenderer
import org.joml.Vector2f
import org.lwjgl.opengl.GL30.*
import java.io.File

// -javaagent:lwjglx-debug-1.0.0.jar
fun main() {
    var fontRenderer: SDFFontRenderer? = null
    var textureRenderer: TextureRenderer? = null
    var frameBuffer: Framebuffer? = null
    val window = object : Window(1280, 720, "Game Of Life") {
        override fun render(deltaTime: Long) {
            glClear(GL_COLOR_BUFFER_BIT)
            glClearColor(.1F, .1F, .1F, 1F)
            frameBuffer!!.bind()
            fontRenderer!!.render("test Text lol yY>1", 0F, 0F)
            fontRenderer!!.render("test Text lol yY>2", 0F, 26F)
            fontRenderer!!.render("test Text lol yY>3", 0F, 1080F)
            fontRenderer!!.render("test Text lol yY>4", 0F, 1080F - 26F)
            fontRenderer!!.finish()
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            val texture = frameBuffer!!.texture
            textureRenderer!!.render(frameBuffer!!.texture, Vector2f(0F, 0F))
            textureRenderer!!.finish()
        }

        override fun postStart() {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
    }
    fontRenderer = SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText()
    ), 26F) // 26
    textureRenderer = TextureRenderer()
    frameBuffer = Framebuffer(1280, 720)
    window.loop()
}
