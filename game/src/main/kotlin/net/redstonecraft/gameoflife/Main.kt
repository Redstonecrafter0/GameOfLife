package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.buffer.Framebuffer
import net.redstonecraft.opengl.camera.OrthographicCamera
import net.redstonecraft.opengl.render.*
import org.joml.Vector2f
import org.lwjgl.opengl.GL30.*
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

// -javaagent:lwjglx-debug-1.0.0.jar
fun main() {
    val sigma = 2F
    val fontRenderer by lazy { SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText(),
    ), 12F, OrthographicCamera(0F, 1280F, 0F, 720F)) } // 26
    val textureRenderer by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val textureRenderer2 by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 720F, 0F)) }
    val frameBuffer by lazy { Framebuffer(1280, 720) }
    val blurPass1 by lazy { Framebuffer(1280 / 2, 720 / 2) }
    val blurPass2 by lazy { Framebuffer(1280 / 2, 720 / 2) }
    val blurRenderer by lazy { HorizontalBlurRenderer(OrthographicCamera(0F, 1280F, 0F, 720F), sigma) }
    val blurRenderer2 by lazy { VerticalBlurRenderer(OrthographicCamera(0F, 1280F, 0F, 720F), sigma) }
    val maskRenderer by lazy { MaskRenderer(OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val bezierRenderer by lazy { BezierRenderer(OrthographicCamera(0F, 1280F, 720F, 0F)) }
    val blurMask by lazy { Texture(ImageIO.read(File("mask.png"))) }
    val lenna by lazy { Texture(ImageIO.read(File("lenna.png"))) }
    val window = object : Window(1280, 720, "Game Of Life") {
        override fun render(deltaTime: Long) {
            frameBuffer.bind()
            glClearColor(.1F, .1F, .1F, 1F)
            glClear(GL_COLOR_BUFFER_BIT)
            fontRenderer.render("test Text lol yY>1", 0F, 0F)
            fontRenderer.render("test Text lol yY>2MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", 0F, 26F)
            fontRenderer.render("test Text lol yY>3", 0F, 720F)
            fontRenderer.render("test Text lol yY>4", 0F, 720F - 26F)
            fontRenderer.finish()
            textureRenderer2.render(lenna, Vector2f(400F, 128F))
            textureRenderer2.finish()
            blurPass1.bind()
            blurRenderer.texture = frameBuffer.texture
            blurRenderer.render(Vector2f(0F, 0F), Vector2f(1280F / 2, 720F / 2))
            blurRenderer.finish()
            blurPass2.bind()
            blurRenderer2.texture = blurPass1.texture
            blurRenderer2.render(Vector2f(0F, 0F), Vector2f(1280F / 2, 720F / 2))
            blurRenderer2.finish()
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glClearColor(0F, 0F, 0F, 0F)
            glClear(GL_COLOR_BUFFER_BIT)
            textureRenderer.render(frameBuffer.texture, Vector2f(0F, 0F))
            textureRenderer.finish()
            maskRenderer.mask = blurMask
            maskRenderer.flipMaskY = true
            maskRenderer.render(blurPass2.texture, Vector2f(0F, 0F), Vector2f(1280F, 720F))
            maskRenderer.finish()
            bezierRenderer.linear(Vector2f(50F, 50F), Vector2f(300F, 100F), 1F, Color.WHITE, segments = 5)
            bezierRenderer.finish()
        }

        override fun postStart() {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glDisable(GL_CULL_FACE)
        }

        override fun onResize(width: Int, height: Int) {
//            frameBuffer.resize(width, height)
        }
    }
    window.loop()
}
