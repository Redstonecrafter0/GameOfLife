package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.buffer.Framebuffer
import net.redstonecraft.opengl.buffer.FramebufferDepthStencil
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
    ), 14F, OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val textureRenderer by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val textureRenderer2 by lazy { TextureRenderer(OrthographicCamera(0F, 1280F, 720F, 0F)) }
    val frameBuffer by lazy { FramebufferDepthStencil(1280, 720) }
    val blurPass1 by lazy { Framebuffer(1280 / 2, 720 / 2) }
    val blurPass2 by lazy { Framebuffer(1280 / 2, 720 / 2) }
    val blurRenderer by lazy { HorizontalBlurRenderer(OrthographicCamera(0F, 1280F, 0F, 720F), sigma) }
    val blurRenderer2 by lazy { VerticalBlurRenderer(OrthographicCamera(0F, 1280F, 0F, 720F), sigma) }
    val maskRenderer by lazy { MaskRenderer(OrthographicCamera(0F, 1280F, 0F, 720F)) }
    val nvgRenderer by lazy { NanoVGRenderer(1280, 720, true).apply {
        loadFont("Jetbrains Mono", File("JetBrainsMonoNL-Regular.ttf").readBytes())
    } }
    val blurMask by lazy { Texture(ImageIO.read(File("mask.png"))) }
    val lenna by lazy { Texture(ImageIO.read(File("lenna.png"))) }
    val svg by lazy { SVGTexture(File("cc.svg"), 100, 100) }
    val window = object : Window(1280, 720, "Game Of Life") {
        override fun render(deltaTime: Long) {
            frameBuffer.bind()
            Framebuffer.clearColorDepthStencil(Color(.1F, .1F, .1F))
            fontRenderer.render("test Text lol yY>1", 0F, 0F)
            fontRenderer.render("test Text lol yY>2MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", 0F, 26F)
            fontRenderer.render("test Text lol yY>3", 0F, 720F)
            fontRenderer.render("test Text lol yY>4", 0F, 720F - 26F)
            fontRenderer.finish()
            textureRenderer2.render(lenna, Vector2f(400F, 128F))
            textureRenderer2.render(svg, Vector2f(200F, 200F))
            textureRenderer2.finish()
            nvgRenderer.render {
                fill(rgb(0F, 1F, 1F)) {
                    circle(50F, 50F, 10F)
                }
                stroke(rgb(0F, 1F, 1F), 5F) {
                    moveTo(100F, 100F)
                    lineTo(150F, 130F)
                    cubicBezierTo(200F, 160F, 200F, 200F, 150F, 190F)
                    arcTo(150F, 200F, 200F, 300F, 50F)
                }
                font("Jetbrains Mono", linearGradient(10F, 80F, 60F, 80F, rgb(0F, 1F, 0F), rgb(1F, 1F, 0F))) {
                    text(10F, 80F, "NanoVG Test Text")
                }
            }
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            blurPass1.bind()
            blurRenderer.texture = frameBuffer.texture
            blurRenderer.render(Vector2f(0F, 0F), Vector2f(1280F / 2, 720F / 2))
            blurRenderer.finish()
            blurPass2.bind()
            blurRenderer2.texture = blurPass1.texture
            blurRenderer2.render(Vector2f(0F, 0F), Vector2f(1280F / 2, 720F / 2))
            blurRenderer2.finish()
            Framebuffer.unbind()
            Framebuffer.clear(Color(0, 0, 0, 0))
            textureRenderer.render(frameBuffer.texture, Vector2f(0F, 0F))
            textureRenderer.finish()
            maskRenderer.mask = blurMask
            maskRenderer.flipMaskY = true
            maskRenderer.render(blurPass2.texture, Vector2f(0F, 0F), Vector2f(1280F, 720F))
            maskRenderer.finish()
        }

        override fun postStart() {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glDisable(GL_CULL_FACE)
        }
    }
    window.loop()
}
