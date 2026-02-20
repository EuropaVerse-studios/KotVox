package engine.player

import engine.world.World
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gestisce la visuale del giocatore e il movimento fisico (camminata, collisioni ecc).
 * Prima era conosciuta come `Camera`.
 */
class Player(private val world: World) {
    // Posizione iniziale provvisoria
    val position = Vector3f(0f, 20f, 0f)
    
    // Rotazione (in gradi)
    var yaw = -90f   // Rotazione sinistra/destra. -90 gradi = asse Z negativo
    var pitch = 0f   // Rotazione su/giù
    
    // --- VARIABILI FISICHE ---
    private var velocityY = 0f
    private val gravity = 25f     // Accelerazione di gravità
    private val jumpForce = 8f    // Forza del salto
    private var isGrounded = false

    // Impostazioni (poi le collegheremo stabilmente alla UI, per ora campi modificabili)
    var walkSpeed = 5f      // Velocità in unità al secondo
    var sensitivity = 0.15f // Sensibilità del mouse
    
    // Matrici per OpenGL
    val viewMatrix = Matrix4f()
    val projectionMatrix = Matrix4f()

    init {
        projectionMatrix.perspective(Math.toRadians(70.0).toFloat(), 1280f / 720f, 0.01f, 1000f)
    }

    /**
     * Aggiorna la matrice di vista in base alla posizione e rotazione correnti.
     */
    fun updateViewMatrix() {
        viewMatrix.identity()
            .rotateX(Math.toRadians(pitch.toDouble()).toFloat())
            .rotateY(Math.toRadians(yaw.toDouble()).toFloat())
            .translate(-position.x, -position.y - playerHeight, -position.z)
            // Modifica: ora la 'position' indica i PIEDI del giocatore. Trasliamo la visuale in alto di 'playerHeight'
    }

    /**
     * Gestisce il movimento con WASD e applica la geometria del terreno.
     * I calcoli sono indipendentemente dagli FPS grazie al parametro deltaTime.
     */
    fun handleInput(window: Long, deltaTime: Float) {
        // --- CALCOLO VETTORI DIREZIONALI ---
        val yawRad = Math.toRadians(yaw.toDouble())

        // Usiamo la vera formula per ottenere "avanti / dietro" rispetto alla telecamera in OpenGL
        val frontX = sin(yawRad).toFloat()
        val frontZ = -cos(yawRad).toFloat()

        // Calcoliamo "destra / sinistra" 
        val rightX = cos(yawRad).toFloat()
        val rightZ = sin(yawRad).toFloat()
        
        val velocity = walkSpeed * deltaTime

        // --- APPLICAZIONE MOVIMENTO LOGICO (X/Z) ---
        var moveX = 0f
        var moveZ = 0f

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            moveX += frontX * velocity
            moveZ += frontZ * velocity
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            moveX -= frontX * velocity
            moveZ -= frontZ * velocity
        }
        
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            moveX += rightX * velocity
            moveZ += rightZ * velocity
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            moveX -= rightX * velocity
            moveZ -= rightZ * velocity
        }

        // --- GESTIONE GRAVITA E SALTO (Y) ---
        if (isGrounded && glfwGetKey(window, GLFW_SPACE) == GLFW_PRESS) {
            velocityY = jumpForce
            isGrounded = false
        }

        velocityY -= gravity * deltaTime
        var moveY = velocityY * deltaTime

        // --- APPLICAZIONE COLLISIONI RUDIMENTALI ---
        // Prima controlliamo X
        position.x += moveX
        if (isColliding(position.x, position.y + 0.1f, position.z) || isColliding(position.x, position.y + playerHeight - 0.1f, position.z)) {
            position.x -= moveX // Annulla movimento se entri in un blocco
        }

        // Poi controlliamo Z
        position.z += moveZ
        if (isColliding(position.x, position.y + 0.1f, position.z) || isColliding(position.x, position.y + playerHeight - 0.1f, position.z)) {
            position.z -= moveZ // Annulla
        }

        // Poi controlliamo Y (verticale)
        position.y += moveY
        isGrounded = false

        // Controllo collisione verso il Basso (Piedi)
        if (moveY < 0 && isColliding(position.x, position.y, position.z)) {
            // L'altezza y intera (sotto)
            position.y = Math.ceil(position.y.toDouble()).toFloat()
            velocityY = 0f
            isGrounded = true
        }
        // Controllo collisione verso l'Alto (Testa)
        else if (moveY > 0 && isColliding(position.x, position.y + playerHeight, position.z)) {
            position.y = Math.floor((position.y + playerHeight).toDouble()).toFloat() - playerHeight
            velocityY = 0f
        }
        
        // Failsafe in caso esca dalla mappa o dal limite inferiore (Void)
        if (position.y < -10f) {
            position.y = 50f
            velocityY = 0f
        }
    }

    /**
     * Helper per controllare se un punto interseca un blocco solido nel mondo.
     */
    private fun isColliding(x: Float, y: Float, z: Float): Boolean {
        // Blocco id 0 = Aria. Tutto il resto al momento è solido.
        return world.getBlockAt(x, y, z).toInt() != 0
    }

    /**
     * Gestisce la rotazione con il mouse per guardarsi attorno.
     */
    fun handleMouseInput(dx: Double, dy: Double) {
        yaw += (dx * sensitivity).toFloat()
        pitch += (dy * sensitivity).toFloat()

        if (pitch > 89f) pitch = 89f
        if (pitch < -89f) pitch = -89f
    }
}
