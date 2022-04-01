package net.redstonecraft.opengl.render

import java.io.Closeable

abstract class BatchRenderer<T : Batch>(val batch: T): Closeable {

    abstract fun uploadUniformShader(shader: ShaderProgram)

    override fun close() {
    }

}
