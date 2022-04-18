package net.redstonecraft.opengl.render

import org.lwjgl.opengl.GL30.*

abstract class Batch(
    val size: Int,
    val shader: ShaderProgram,
    vararg attributes: Int
) {

    val vertSize = attributes.sum()

    var count = 0

    open fun postVao(id: Int) {}
    open fun postVbo(id: Int) {
        glBufferData(GL_ARRAY_BUFFER, (Float.SIZE_BYTES * vertSize * size).toLong(), GL_DYNAMIC_DRAW)
    }
    open fun postEbo(id: Int) {}

    val vao = glGenVertexArrays().apply {
        glBindVertexArray(this)
    }

    init {
        postVao(vao)
    }

    val vbo = glGenBuffers().apply {
        glBindBuffer(GL_ARRAY_BUFFER, this)
    }

    init {
        postVbo(vbo)
    }

    val ebo = glGenBuffers().apply {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this)
    }

    init {
        postEbo(ebo)
    }

    init {
        val stride = Float.SIZE_BYTES * vertSize
        var pos = 0
        for ((i, v) in attributes.withIndex()) {
            glVertexAttribPointer(i, v, GL_FLOAT, false, stride, (pos * Float.SIZE_BYTES).toLong())
            glEnableVertexAttribArray(i)
            pos += v
        }
    }

    fun flush() {
        bufferData()
        shader.bind()
        upload(shader)
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, count * vertSize, GL_UNSIGNED_INT, 0)
        count = 0
    }

    fun buffer(target: Int, id: Int, size: Long, data: FloatArray, usage: Int, sub: Boolean = true) {
        glBindBuffer(target, id)
        if (sub) {
            glBufferSubData(target, 0, data)
            glBufferData(target, size, usage)
        } else {
            glBufferData(target, data, usage)
        }
    }
    fun buffer(target: Int, id: Int, size: Long, data: IntArray, usage: Int, sub: Boolean = true) {
        glBindBuffer(target, id)
        if (sub) {
            glBufferData(target, size, usage)
            glBufferSubData(target, 0, data)
        } else {
            glBufferData(target, data, usage)
        }
    }
    fun bufferVbo(size: Long, data: FloatArray, usage: Int, sub: Boolean = true) = buffer(GL_ARRAY_BUFFER, vbo, size, data, usage, sub)
    fun bufferEbo(size: Long, data: IntArray, usage: Int, sub: Boolean = true) = buffer(GL_ELEMENT_ARRAY_BUFFER, ebo, size, data, usage, sub)

    //(Float.SIZE_BYTES * vertSize * size).toLong()

    abstract fun upload(shader: ShaderProgram)
    abstract fun bufferData()

}
