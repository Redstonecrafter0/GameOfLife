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
            glClear(GL_COLOR)
            glClearColor(0F, 0F, 0F, 1F)
            renderer!!.render("testasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios\ntestasdhpoaihdsoidoaihsdoioaiphdpaidpahios", -.5F, -.5F, bgColor = Color.WHITE)
            renderer!!.finish()
        }
    }
    renderer = SDFFontRenderer(SDFFont(
        File("image.png").readBytes(),
        File("atlas.json").readText()
    ))
    window.loop()
}
