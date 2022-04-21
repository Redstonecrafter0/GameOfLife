package net.redstonecraft.opengl

import net.redstonecraft.opengl.interfaces.LPointed
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil.NULL

open class Window(width: Int, height: Int, title: String, vsync: Boolean = true) : LPointed {

    companion object {
        init {
            if (!glfwInit()) throw RuntimeException("Error initializing GLFW")
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        }
    }

    final override val pointer = glfwCreateWindow(width, height, title, NULL, NULL)

    init {
        if (pointer == NULL) throw RuntimeException("Failed creating window")
        glfwSetKeyCallback(pointer, object : GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                if (window == pointer) onKey(key, scancode, action, mods)
            }
        })
        glfwSetCharCallback(pointer, object : GLFWCharCallback() {
            override fun invoke(window: Long, key: Int) {
                if (window == pointer) onChar(key)
            }
        })
        glfwSetCursorPosCallback(pointer, object : GLFWCursorPosCallback() {
            override fun invoke(window: Long, xpos: Double, ypos: Double) {
                if (window == pointer) onMouseMove(xpos, ypos)
            }
        })
        glfwSetMouseButtonCallback(pointer, object : GLFWMouseButtonCallback() {
            override fun invoke(window: Long, key: Int, action: Int, mods: Int) {
                if (window == pointer) onMouse(key, action, mods)
            }
        })
        glfwSetScrollCallback(pointer, object : GLFWScrollCallback() {
            override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                if (window == pointer) onScroll(xoffset, yoffset)
            }
        })
        glfwSetWindowSizeCallback(pointer, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                if (window == pointer) onResize(width, height)
            }
        })
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        glfwSetWindowPos(pointer, vidMode.width() / 2 - width / 2, vidMode.height() / 2 - height / 2)
        glfwMakeContextCurrent(pointer)
        if (vsync) glfwSwapInterval(1)
        glfwSetInputMode(pointer, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
        glfwShowWindow(pointer)
        GL.createCapabilities()
    }

    fun loop() {
        postStart()
        var end: Long = System.nanoTime()
        var oldStart = System.nanoTime()
        while (!glfwWindowShouldClose(pointer)) {
            val start = System.nanoTime()
            render(end - oldStart)
            end = System.nanoTime()
            oldStart = start
            glfwSwapBuffers(pointer)
            glfwPollEvents()
        }
        glfwDestroyWindow(pointer)
    }

    /**
     * @param deltaTime in nanoseconds
     * */
    open fun render(deltaTime: Long) {}

    open fun postStart() {}

    open fun onKey(key: Int, scancode: Int, action: Int, mods: Int) {}
    open fun onChar(key: Int) {}
    open fun onMouseMove(xPox: Double, yPos: Double) {}
    open fun onMouse(key: Int, action: Int, mods: Int) {}
    open fun onScroll(xOffset: Double, yOffset: Double) {}
    open fun onResize(width: Int, height: Int) {}
}
