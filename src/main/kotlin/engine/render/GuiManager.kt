package engine.render

import engine.core.GameState
import engine.core.KotVox
import engine.modding.ModManager
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw

/**
 * Gestisce l'interfaccia utente in sovrimpressione tramite ImGui.
 */
class GuiManager(private val windowId: Long, private val engine: KotVox) {

    private val imGuiGlfw = ImGuiImplGlfw()
    private val imGuiGl3 = ImGuiImplGl3()

    fun init() {
        ImGui.createContext()
        val io = ImGui.getIO()
        // Abilita la tastiera nella UI
        io.iniFilename = null // Disabilita la creazione del file imgui.ini

        // Imposta lo stile di ImGui a qualcosa di scuro e moderno
        ImGui.styleColorsDark()

        // Inizializza i bridge per GLFW e OpenGL 3
        imGuiGlfw.init(windowId, true)
        imGuiGl3.init("#version 330")
    }

    /**
     * Da chiamare all'inizio di ogni frame per preparare la UI.
     */
    fun startFrame() {
        imGuiGlfw.newFrame()
        ImGui.newFrame()
    }

    /**
     * Disegna gli elementi UI in base allo stato attuale del gioco.
     */
    fun renderUi(currentState: GameState) {
        when (currentState) {
            GameState.MAIN_MENU -> drawMainMenu()
            GameState.MOD_SCREEN -> drawModScreen()
            GameState.PLAYING -> drawHud()
        }
    }

    /**
     * Da chiamare alla fine di ogni frame per inviare tutto alla scheda video.
     */
    fun endFrame() {
        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
    }

    /**
     * Libera le risorse di ImGui alla chiusura.
     */
    fun destroy() {
        imGuiGl3.dispose()
        imGuiGlfw.dispose()
        ImGui.destroyContext()
    }

    // --- FINESTRE UI SPECIFICHE ---

    private fun drawMainMenu() {
        ImGui.setNextWindowPos(1280f / 2 - 150f, 720f / 2 - 100f)
        ImGui.setNextWindowSize(300f, 200f)
        
        // Finestra non ridimensionabile, senza titolo, non muovibile
        val flags = ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoSavedSettings
        
        if (ImGui.begin("Main Menu", flags)) {
            // Testo centrato alla bell'e meglio
            ImGui.setCursorPosX(100f)
            ImGui.text("KOTVOX")
            ImGui.spacing(); ImGui.spacing(); ImGui.spacing()

            // Centriamo i bottoni
            ImGui.setCursorPosX(50f)
            if (ImGui.button("Gioca nel Mondo", 200f, 30f)) {
                engine.changeState(GameState.PLAYING)
            }
            
            ImGui.spacing()
            ImGui.setCursorPosX(50f)
            if (ImGui.button("Gestione Mods", 200f, 30f)) {
                engine.changeState(GameState.MOD_SCREEN)
            }
            
            ImGui.spacing()
            ImGui.setCursorPosX(50f)
            if (ImGui.button("Esci dal Gioco", 200f, 30f)) {
                engine.quit()
            }
        }
        ImGui.end()
    }

    private fun drawModScreen() {
        ImGui.setNextWindowPos(1280f / 2 - 250f, 720f / 2 - 200f)
        ImGui.setNextWindowSize(500f, 400f)
        
        val flags = ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove
        
        if (ImGui.begin("Gestione Mod Installate", flags)) {
            val mods = ModManager.getLoadedMods() // Dovremo creare questo getter in ModManager
            
            if (mods.isEmpty()) {
                ImGui.text("Non ci sono Mod installate al momento.")
            } else {
                for (mod in mods) {
                    ImGui.bulletText("${mod.name} (v${mod.version}) - ID: ${mod.modId}")
                }
            }

            ImGui.spacing(); ImGui.spacing()
            
            // Bottone Indietro in basso
            ImGui.setCursorPosY(350f)
            if (ImGui.button("Torna al Menu Principale", 200f, 30f)) {
                engine.changeState(GameState.MAIN_MENU)
            }
        }
        ImGui.end()
    }

    private fun drawHud() {
        // Un semplice HUD testuale (come in Minecraft in alto a sinistra, se vuoi, sennò F3 basta).
        // Per ora lo lasciamo vuoto, perché usiamo lo stato title per il debug o log
    }
}
