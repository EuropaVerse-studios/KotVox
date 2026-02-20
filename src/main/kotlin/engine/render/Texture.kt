package engine.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

/**
 * Carica e gestisce una Texture OpenGL.
 */
class Texture(filePath: String) {
    val id: Int

    init {
        id = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, id)

        // Impostazioni di wrapping (ripetizione della texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        // Impostazioni di filtraggio (Sharp per i pixel art, o Linear per smooth)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            stbi_set_flip_vertically_on_load(true)
            val data: ByteBuffer? = stbi_load(filePath, w, h, channels, 4)
            if (data == null) {
                throw RuntimeException("Impossibile caricare la texture: $filePath. Errore: " + stbi_failure_reason())
            }

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
            glGenerateMipmap(GL_TEXTURE_2D)

            stbi_image_free(data)
        }
    }

    fun bind() = glBindTexture(GL_TEXTURE_2D, id)
    fun unbind() = glBindTexture(GL_TEXTURE_2D, 0)
    
    fun delete() = glDeleteTextures(id)
}
