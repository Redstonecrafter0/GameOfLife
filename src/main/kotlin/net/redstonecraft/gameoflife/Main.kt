package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.render.SDFFont
import net.redstonecraft.opengl.render.SDFFontRenderer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.io.File

fun main() {
    var renderer: SDFFontRenderer? = null
    val window = object : Window(1280, 720, "Game Of Life") {
        override fun render(deltaTime: Long) {
            glClear(GL_COLOR_BUFFER_BIT)
            glClearColor(.1F, .1F, .1F, 1F)
            renderer!!.render("test text", 0F, 0F, bgColor = Color.WHITE)
            renderer!!.finish()
        }

        override fun postStart() {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
    }
    renderer = SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText()
    ), 500F)
    window.loop()
}
