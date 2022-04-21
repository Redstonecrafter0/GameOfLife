package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.buffer.Framebuffer
import net.redstonecraft.opengl.camera.OrthographicCamera
import net.redstonecraft.opengl.render.*
import org.joml.Vector2f
import org.lwjgl.opengl.GL30.*
import java.io.File
import javax.imageio.ImageIO

// -javaagent:lwjglx-debug-1.0.0.jar
fun main() {
    val fontRenderer: SDFFontRenderer by lazy { SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText(),
    ), 12F, OrthographicCamera(0F, 1280F, 0F, 720F)) } // 26
    val textureRenderer: TextureRenderer by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val textureRenderer2: TextureRenderer by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 720F, 0F)) }
    val frameBuffer: Framebuffer by lazy { Framebuffer(1920, 1080) }
    val lenna: Texture by lazy { Texture(ImageIO.read(File("lenna.png")), 512, 512) }
    val window = object : Window(1280, 720, "Game Of Life") {
        override fun render(deltaTime: Long) {
            frameBuffer.bind()
            glClearColor(.1F, .1F, .1F, 1F)
            glClear(GL_COLOR_BUFFER_BIT)
            fontRenderer.render("test Text lol yY>1", 0F, 0F)
            fontRenderer.render("test Text lol yY>2MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", 0F, 26F)
            fontRenderer.render("test Text lol yY>3", 0F, 720F)
            fontRenderer.render("test Text lol yY>4", 0F, 720F - 26F)
            fontRenderer.finish()
            textureRenderer2.render(lenna, Vector2f(400F, 128F))
            textureRenderer2.finish()
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glClearColor(0F, 0F, 0F, 0F)
            glClear(GL_COLOR_BUFFER_BIT)
            textureRenderer.render(frameBuffer.texture, Vector2f(0F, 0F))
            textureRenderer.finish()
        }

        override fun postStart() {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }

        override fun onResize(width: Int, height: Int) {
            frameBuffer.resize(width, height)
        }
    }
    window.loop()
}
