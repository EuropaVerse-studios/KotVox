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
    
    // Parametri di movimento
    private val walkSpeed = 5f      // Velocità in unità al secondo
    private val sensitivity = 0.15f // Sensibilità del mouse
    private val playerHeight = 1.6f // Altezza degli occhi del giocatore rispetto ai piedi
    
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
            .translate(-position.x, -position.y, -position.z)
    }

    /**
     * Gestisce il movimento con WASD e applica la geometria del terreno.
     * I calcoli sono indipendentemente dagli FPS grazie al parametro deltaTime.
     */
    fun handleInput(window: Long, deltaTime: Float) {
        // --- CALCOLO VETTORI DIREZIONALI ---
        // front (dove guardiamo fisicamente sull'asse orizzontale)
        val yawRad = Math.toRadians(yaw.toDouble())

        // Usiamo la vera formula vettoriale per ottenere "avanti / dietro"
        val frontX = cos(yawRad).toFloat()
        val frontZ = sin(yawRad).toFloat()

        // Calcoliamo "destra / sinistra" ruotando il vettore 'front' di 90 gradi.
        // E' matematicamente definito come sin per X e -cos per Z.
        val rightX = sin(yawRad).toFloat()
        val rightZ = -cos(yawRad).toFloat()
        
        val velocity = walkSpeed * deltaTime

        // --- APPLICAZIONE MOVIMENTO LOGICO ---
        // WASD: Avanti/Dietro usa i vettori Front. Destra/Sinistra usa i vettori Right.
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            position.x += frontX * velocity
            position.z += frontZ * velocity
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            position.x -= frontX * velocity
            position.z -= frontZ * velocity
        }
        
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            position.x += rightX * velocity
            position.z += rightZ * velocity
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            position.x -= rightX * velocity
            position.z -= rightZ * velocity
        }
        
        // --- COLLISIONE CON IL TERRENO (GRAVITA') ---
        // Ora leggiamo l'altezza dal nostro World in quel punto esatto.
        // Siccome il nostro generatore restituisce Y pieno del terreno,
        // andiamo a spawnare gli occhi ad altezza: terreno + playerHeight.
        val groundY = world.getGroundHeight(position.x, position.z).toFloat()
        val targetY = groundY + playerHeight
        
        // Applica l'altezza forzata del terreno
        position.y = targetY
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
