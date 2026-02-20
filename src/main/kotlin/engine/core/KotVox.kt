package engine.core

import engine.core.GameState
import engine.player.Player
import engine.registry.Block
import engine.registry.Registry
import engine.modding.ModManager
import engine.render.ChunkMesher
import engine.render.GuiManager
import engine.render.Shader
import engine.util.PerformanceTracker
import engine.world.Chunk
import engine.world.World
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.io.File

/**
 * Contenitore per i dati della GPU di un Chunk.
 * Salva l'ID del Vertex Array Object e il numero di vertici.
 */
class RenderableChunk(val chunk: Chunk, val vaoId: Int, val vertexCount: Int)

/**
 * Questa è la classe principale del motore Voxel.
 * Gestisce la creazione della finestra, il loop di gioco e l'inizializzazione di OpenGL.
 */
class KotVox {

    private var window: Long = 0
    private val performance = PerformanceTracker()
    
    // Inizia la gestione di Mondo e Giocatore separati
    private val world = World()
    private val player = Player(world)
    
    // Gestione Stati e Interfaccia Utente
    var currentState = GameState.MAIN_MENU
        private set
    private lateinit var guiManager: GuiManager
    
    // Per uscire
    private var isQuitRequested = false
    
    private lateinit var shader: Shader
    private val renderableChunks = mutableListOf<RenderableChunk>()
    
    // Per il mouse
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var firstMouse = true
    
    // Per la gestione del debug
    private var isDebugMenuOpen = false
    private var f3KeyPressed = false

    fun run() {
        println("Avvio KotVox...")
        println("[INFO] Premi il tasto 'F3' per attivare il Debug Menu.")

        // 1. CARICAMENTO MOD (Modding-First!)
        ModManager.loadMods()

        // 2. REGISTRAZIONE BLOCCHI CORE
        setupCoreBlocks()
        
        // 3. REGISTRAZIONE BLOCCHI MOD
        ModManager.initModRegistries()

        // 4. INIZIALIZZAZIONE GRAFICA E LOOP
        init()
        loop()

        // Distrugge la interfaccia grafica prima della finestra
        guiManager.destroy()

        // Distrugge la finestra e pulisce le risorse alla chiusura
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Termina GLFW e rilascia il callback di errore
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) {
            throw IllegalStateException("Impossibile inizializzare GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(1280, 720, "KotVox (Hytale Like)", NULL, NULL)
        if (window == NULL) {
            throw RuntimeException("Impossibile creare la finestra GLFW")
        }

        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                // Se stiamo giocando, ESC riapre il menu base
                if (currentState == GameState.PLAYING) {
                    changeState(GameState.MAIN_MENU)
                }
            }
            
            // Gestione del menu di debug via F3 (Solo se stiamo giocando)
            if (currentState == GameState.PLAYING) {
                if (key == GLFW_KEY_F3 && action == GLFW_PRESS) {
                    f3KeyPressed = true
                } else if (key == GLFW_KEY_F3 && action == GLFW_RELEASE && f3KeyPressed) {
                    isDebugMenuOpen = !isDebugMenuOpen
                    f3KeyPressed = false
                    
                    if (!isDebugMenuOpen) {
                        glfwSetWindowTitle(window, "KotVox")
                    }
                }
            }
        }

        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        if (videoMode != null) {
            glfwSetWindowPos(window, (videoMode.width() - 1280) / 2, (videoMode.height() - 720) / 2)
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)

        // Inizializza il GuiManager per ImGui ORA (dopo che il contesto esiste)
        guiManager = GuiManager(window, this)
        guiManager.init()

        // Settiamo il cursore in base allo stato base
        updateCursorState()
        
        // Callback per il mouse
        glfwSetCursorPosCallback(window) { _, xpos, ypos ->
            if (firstMouse) {
                lastMouseX = xpos
                lastMouseY = ypos
                firstMouse = false
            }
            val dx = xpos - lastMouseX
            val dy = lastMouseY - ypos 
            lastMouseX = xpos
            lastMouseY = ypos
            
            // La camera ruota SOLO se stiamo effettivamente giocando,
            // altrimenti serve al mouse per cliccare sulla UI!
            if (currentState == GameState.PLAYING) {
                player.handleMouseInput(dx, dy)
            }
        }

        GL.createCapabilities()

        // Carica gli shader
        val vert = File("src/main/resources/shader.vert").readText()
        val frag = File("src/main/resources/shader.frag").readText()
        shader = Shader(vert, frag)

        setupTestWorld()

        glClearColor(0.4f, 0.7f, 1.0f, 0.0f)
        glEnable(GL_DEPTH_TEST)
    }

    private fun setupTestWorld() {
        // Applichiamo la delega al World per generare la mappa in memoria
        val chunksToRender = world.initStartMap()
        
        for (chunk in chunksToRender) {
            // Generiamo la mesh usando il nostro sistema di Face Culling
            val buffer = ChunkMesher.generateMesh(chunk)
            val vertexCount = buffer.remaining() / 6 // 6 float per vertice (3 pos + 3 col)
            
            // Se il chunk non è vuoto, lo carichiamo nella GPU
            if (vertexCount > 0) {
                val vaoId = glGenVertexArrays()
                val vboId = glGenBuffers()

                glBindVertexArray(vaoId)
                glBindBuffer(GL_ARRAY_BUFFER, vboId)
                
                // Carichiamo i dati nella GPU
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
                
                // IMPORTANTE: Liberiamo la memoria off-heap dopo l'upload!
                MemoryUtil.memFree(buffer)

                // Spieghiamo a OpenGL come leggere l'array: 3 float per X,Y,Z e 3 float per R,G,B
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0)
                glEnableVertexAttribArray(0)
                glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4)
                glEnableVertexAttribArray(1)
                
                // Aggiungiamo il chunk pronto alla nostra lista
                renderableChunks.add(RenderableChunk(chunk, vaoId, vertexCount))
            } else {
                // Anche se è vuoto, dobbiamo liberare il buffer!
                MemoryUtil.memFree(buffer)
            }
        }
    }

    private fun loop() {
        var lastTime = glfwGetTime()

        while (!glfwWindowShouldClose(window) && !isQuitRequested) {
            val currentTime = glfwGetTime()
            val deltaTime = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            // Prepariamo ImGui per questo ciclo frame
            guiManager.startFrame()

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // --- FASE 1: GIOCO 3D ---
            if (currentState == GameState.PLAYING) {
                performance.update(window)
                player.handleInput(window, deltaTime)
                player.updateViewMatrix()

                if (isDebugMenuOpen) {
                    val pos = player.position
                    val debugStr = String.format("KotVox [DEBUG] - X: %.1f, Y: %.1f, Z: %.1f", pos.x, pos.y, pos.z)
                    glfwSetWindowTitle(window, debugStr)
                }

                shader.use()
                
                val viewBuf = BufferUtils.createFloatBuffer(16)
                player.viewMatrix.get(viewBuf)
                glUniformMatrix4fv(shader.getUniformLocation("view"), false, viewBuf)

                val projBuf = BufferUtils.createFloatBuffer(16)
                player.projectionMatrix.get(projBuf)
                glUniformMatrix4fv(shader.getUniformLocation("projection"), false, projBuf)

                for (rc in renderableChunks) {
                    val chunkWorldX = rc.chunk.x * Chunk.SIZE.toFloat()
                    val chunkWorldY = rc.chunk.y * Chunk.SIZE.toFloat()
                    val chunkWorldZ = rc.chunk.z * Chunk.SIZE.toFloat()
                    
                    val model = Matrix4f().translate(chunkWorldX, chunkWorldY, chunkWorldZ)
                    val modelBuf = BufferUtils.createFloatBuffer(16)
                    model.get(modelBuf)
                    glUniformMatrix4fv(shader.getUniformLocation("model"), false, modelBuf)

                    glBindVertexArray(rc.vaoId)
                    glDrawArrays(GL_TRIANGLES, 0, rc.vertexCount)
                }
            } else {
                // Resetta il titolo se siamo in un Menu
                glfwSetWindowTitle(window, "KotVox Menu")
            }

            // --- FASE 2: INTERFACCIA (UI) ---
            // Disegna e stampa a schermo la UI sopra a tutto
            guiManager.renderUi(currentState)
            guiManager.endFrame()

            glfwSwapBuffers(window)
            glfwPollEvents()
        }
    }

    // --- COMANDI ESTERNI PER L'UI ---
    
    fun changeState(newState: GameState) {
        currentState = newState
        updateCursorState()
    }
    
    fun quit() {
        isQuitRequested = true
    }
    
    private fun updateCursorState() {
        if (currentState == GameState.PLAYING) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        } else {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
        }
    }

    private fun setupCoreBlocks() {
        Registry.registerBlock("engine", "air", Block("air", "Aria", true))
        Registry.registerBlock("engine", "stone", Block("stone", "Pietra"))
        Registry.registerBlock("engine", "grass", Block("grass", "Erba"))
        Registry.listBlocks()
    }
}

fun main() {
    KotVox().run()
}
