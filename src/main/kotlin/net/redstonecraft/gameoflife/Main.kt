package net.redstonecraft.gameoflife

import net.redstonecraft.opengl.Window
import net.redstonecraft.opengl.render.SDFFont
import net.redstonecraft.opengl.render.SDFFontRenderer
import java.io.File

fun main() {
    val renderer = SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText()
    ))
    object : Window(480, 360, "Test") {
        override fun render(deltaTime: Long) {
        }
    }.loop()
}
