package engine.util

import org.lwjgl.glfw.GLFW.glfwSetWindowTitle
import org.lwjgl.glfw.GLFW.glfwGetTime

/**
 * Questa classe monitora le prestazioni del gioco in tempo reale.
 */
class PerformanceTracker {
    private var lastTime: Double = glfwGetTime()
    private var frameCount: Int = 0
    private var fps: Int = 0
    private var frameTime: Double = 0.0

    /**
     * Da chiamare ad ogni frame. Calcola FPS e Frame Time.
     * @param window L'ID della finestra GLFW per aggiornare il titolo.
     */
    fun update(window: Long) {
        val currentTime = glfwGetTime()
        frameCount++

        // Se è passato almeno un secondo, aggiorniamo gli FPS
        if (currentTime - lastTime >= 1.0) {
            fps = frameCount
            frameTime = 1000.0 / frameCount.toDouble()
            
            // Aggiorna il titolo della finestra con i dati
            val title = String.format("KotVox | FPS: %d | Frame Time: %.2f ms", fps, frameTime)
            glfwSetWindowTitle(window, title)

            frameCount = 0
            lastTime = currentTime
        }
    }
}
