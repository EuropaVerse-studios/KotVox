package engine.core

/**
 * Definisce lo stato attuale del gioco per capire cosa renderizzare e quali
 * input accettare (UI del menù vs Movimento 3D).
 */
enum class GameState {
    /**
     * Schermata principale: "Gioca", "Gestione Mod", "Esci"
     */
    MAIN_MENU,
    
    /**
     * Schermata che lista le Mod attive nel ModManager
     */
    MOD_SCREEN,
    
    /**
     * Il giocatore è nel mondo 3D, muove la telecamera.
     */
    PLAYING
}
