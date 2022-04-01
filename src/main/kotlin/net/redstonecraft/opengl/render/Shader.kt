package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.interfaces.Pointed
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL31.*
import java.io.Closeable
import java.nio.IntBuffer

class ShaderProgram(vertexShader: VertexShader, fragmentShader: FragmentShader) : Pointed, Closeable {

    override val pointer = glCreateProgram()
    val uniform = mutableMapOf<String, Int>()

    init {
        try {
            vertexShader.attach()
            fragmentShader.attach()
            glLinkProgram(pointer)
            val status = glGetProgrami(pointer, GL_LINK_STATUS)
            val len = glGetProgrami(pointer, GL_INFO_LOG_LENGTH)
            val log = glGetProgramInfoLog(pointer, len)
            println(log)
            if (status == GL_FALSE) throw RuntimeException("Could not link shader")
            val uniformLen = glGetProgrami(pointer, GL_ACTIVE_UNIFORMS)
            val strLen = glGetProgrami(pointer, GL_ACTIVE_UNIFORM_MAX_LENGTH)
            for (i in 0 until uniformLen) {
                val name: String = glGetActiveUniform(pointer, i, strLen, null as IntBuffer?, null as IntBuffer?)
                uniform[name] = glGetUniformLocation(pointer, name)
            }
        } catch (e: RuntimeException) {
            close()
            throw e
        }
    }

    fun uploadTexture(name: String, texture: Texture, slot: Int = 0) {
        bind()
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_BUFFER, texture.pointer)
        glUniform1i(uniform[name]!!, slot)
    }

    fun uploadUniformVec2f(name: String, vec: Vector2f) {
        bind()
        glUniform2fv(uniform[name]!!, floatArrayOf(vec.x, vec.y))
    }
    fun uploadUniformVec3f(name: String, vec: Vector3f) {
        bind()
        glUniform3fv(uniform[name]!!, floatArrayOf(vec.x, vec.y, vec.z))
    }
    fun uploadUniformVec4f(name: String, vec: Vector4f) {
        bind()
        glUniform4fv(uniform[name]!!, floatArrayOf(vec.x, vec.y, vec.z, vec.w))
    }

    fun uploadUniformMat2f(name: String, mat: Matrix2f) {
        bind()
        val buffer = BufferUtils.createFloatBuffer(4)
        mat.get(buffer)
        glUniformMatrix2fv(uniform[name]!!, false, buffer)
    }
    fun uploadUniformMat3f(name: String, mat: Matrix3f) {
        bind()
        val buffer = BufferUtils.createFloatBuffer(9)
        mat.get(buffer)
        glUniformMatrix3fv(uniform[name]!!, false, buffer)
    }
    fun uploadUniformMat4f(name: String, mat: Matrix4f) {
        bind()
        val buffer = BufferUtils.createFloatBuffer(16)
        mat.get(buffer)
        glUniformMatrix4fv(uniform[name]!!, false, buffer)
    }

    fun uploadUniformFloat(name: String, value: Float) {
        bind()
        glUniform1f(uniform[name]!!, value)
    }

    fun bind() {
        glUseProgram(pointer)
    }

    override fun close() {
        glDeleteProgram(pointer)
    }

}

abstract class Shader(source: String, private val type: Int) : Pointed, Closeable {

    final override val pointer = glCreateShader(type)

    init {
        try {
            glShaderSource(pointer, source)
            glCompileShader(pointer)
            val status = glGetShaderi(pointer, GL_COMPILE_STATUS)
            val len = glGetShaderi(pointer, GL_INFO_LOG_LENGTH)
            val log = glGetShaderInfoLog(pointer, len)
            println(log)
            if (status == GL_FALSE) throw RuntimeException("Could not compile shader")
            attach()
        } catch (e: RuntimeException) {
            close()
            throw e
        }
    }

    fun attach() {
        glAttachShader(pointer, type)
    }

    final override fun close() {
        glDeleteShader(pointer)
    }

}

class VertexShader(source: String) : Shader(source, GL_VERTEX_SHADER)
class FragmentShader(source: String) : Shader(source, GL_FRAGMENT_SHADER)
