package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.interfaces.Pointed
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL31.*
import java.io.Closeable

class ShaderProgram(vertexShader: VertexShader, fragmentShader: FragmentShader) : Pointed, Closeable {

    override val pointer = glCreateProgram()
    val uniform = mutableMapOf<String, Int>()

    init {
        try {
            glAttachShader(pointer, vertexShader.pointer)
            glAttachShader(pointer, fragmentShader.pointer)
            glLinkProgram(pointer)
            val status = glGetProgrami(pointer, GL_LINK_STATUS)
            val len = glGetProgrami(pointer, GL_INFO_LOG_LENGTH)
            val log = glGetProgramInfoLog(pointer, len)
            println(log)
            if (status == GL_FALSE) throw RuntimeException("Could not link shader")
            val uniformLen = glGetProgrami(pointer, GL_ACTIVE_UNIFORMS)
            val strLen = glGetProgrami(pointer, GL_ACTIVE_UNIFORM_MAX_LENGTH)
            for (i in 0 until uniformLen) {
                val name = glGetActiveUniform(pointer, i, strLen, BufferUtils.createIntBuffer(1), BufferUtils.createIntBuffer(1))
                uniform[name] = glGetUniformLocation(pointer, name)
            }
        } catch (e: RuntimeException) {
            close()
            throw e
        }
    }

    fun uploadUTexture(name: String, texture: Texture, slot: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_2D, texture.pointer)
        glUniform1i(uniform[name]!!, slot)
    }
    fun uploadUTextures(name: String, vararg texture: Texture, offset: Int = 0) {
        for ((i, t) in texture.withIndex()) {
            glActiveTexture(GL_TEXTURE0 + offset + i)
            glBindTexture(GL_TEXTURE_2D, t.pointer)
        }
        glUniform1iv(uniform["${name}[0]"]!!, IntArray(texture.size) { it + offset })
    }

    fun uploadUVec2f(name: String, vec: Vector2f) {
        glUniform2fv(uniform[name]!!, floatArrayOf(vec.x, vec.y))
    }
    fun uploadUVec3f(name: String, vec: Vector3f) {
        glUniform3fv(uniform[name]!!, floatArrayOf(vec.x, vec.y, vec.z))
    }
    fun uploadUVec4f(name: String, vec: Vector4f) {
        glUniform4fv(uniform[name]!!, floatArrayOf(vec.x, vec.y, vec.z, vec.w))
    }

    fun uploadUMat2f(name: String, mat: Matrix2f) {
        val buffer = BufferUtils.createFloatBuffer(4)
        mat.get(buffer)
        glUniformMatrix2fv(uniform[name]!!, false, buffer)
    }
    fun uploadUMat3f(name: String, mat: Matrix3f) {
        val buffer = BufferUtils.createFloatBuffer(9)
        mat.get(buffer)
        glUniformMatrix3fv(uniform[name]!!, false, buffer)
    }
    fun uploadUMat4f(name: String, mat: Matrix4f) {
        val buffer = BufferUtils.createFloatBuffer(16)
        mat.get(buffer)
        glUniformMatrix4fv(uniform[name]!!, false, buffer)
    }

    fun uploadUFloat(name: String, value: Float) {
        glUniform1f(uniform[name]!!, value)
    }
    fun uploadUInt(name: String, value: Int) {
        glUniform1i(uniform[name]!!, value)
    }
    fun uploadUUInt(name: String, value: Int) {
        glUniform1ui(uniform[name]!!, value)
    }
    fun uploadUBoolean(name: String, value: Boolean) {
        uploadUInt(name, if (value) 1 else 0)
    }

    fun uploadUFloatArray(name: String, value: FloatArray) {
        glUniform1fv(uniform["${name}[0]"]!!, value)
    }
    fun uploadUIntArray(name: String, value: IntArray) {
        glUniform1iv(uniform["${name}[0]"]!!, value)
    }
    fun uploadUBooleanArray(name: String, value: BooleanArray) {
        glUniform1iv(uniform["${name}[0]"]!!, value.map { if (it) 1 else 0 }.toIntArray())
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
            if (status == GL_FALSE) throw RuntimeException("Could not compile ${if (type == GL_VERTEX_SHADER) "vertex" else "fragment"} shader")
        } catch (e: RuntimeException) {
            close()
            throw e
        }
    }

    final override fun close() {
        glDeleteShader(pointer)
    }

}

class VertexShader(source: String) : Shader(source, GL_VERTEX_SHADER)
class FragmentShader(source: String) : Shader(source, GL_FRAGMENT_SHADER)
