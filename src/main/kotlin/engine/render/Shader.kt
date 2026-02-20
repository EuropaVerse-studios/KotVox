package engine.render

import org.lwjgl.opengl.GL20.*

/**
 * Gestisce il caricamento e l'uso degli Shader (programmi che girano sulla GPU).
 */
class Shader(vertexSource: String, fragmentSource: String) {
    private val programId: Int

    init {
        val vertexShaderId = compileShader(vertexSource, GL_VERTEX_SHADER)
        val fragmentShaderId = compileShader(fragmentSource, GL_FRAGMENT_SHADER)

        programId = glCreateProgram()
        glAttachShader(programId, vertexShaderId)
        glAttachShader(programId, fragmentShaderId)
        glLinkProgram(programId)

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw RuntimeException("Errore nel linking dello shader: " + glGetProgramInfoLog(programId))
        }

        glDetachShader(programId, vertexShaderId)
        glDetachShader(programId, fragmentShaderId)
        glDeleteShader(vertexShaderId)
        glDeleteShader(fragmentShaderId)
    }

    fun use() = glUseProgram(programId)
    fun stop() = glUseProgram(0)
    
    fun getUniformLocation(name: String) = glGetUniformLocation(programId, name)

    private fun compileShader(source: String, type: Int): Int {
        val id = glCreateShader(type)
        glShaderSource(id, source)
        glCompileShader(id)
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw RuntimeException("Errore nella compilazione dello shader: " + glGetShaderInfoLog(id))
        }
        return id
    }
}
