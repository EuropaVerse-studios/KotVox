package engine.mods.examplemod

import engine.modding.IMod
import engine.registry.Block
import engine.registry.Registry

/**
 * Questa è una Mod di esempio!
 * Viene caricata in memoria dal ModManager dinamicamente e non sa nulla
 * del loop principale di KotVox (KotVox.kt) o del rendering.
 */
class ExampleMod : IMod {

    // Rispettiamo l patto dell'interfaccia fornendo i dati identificativi
    override val modId: String = "examplemod"
    override val name: String = "Example Mod"
    override val version: String = "1.0.0"

    override fun onInitialize() {
        // Qui potremmo caricare la configurazione, creare finestre extra, ecc.
        println("[$name] Inizializzazione completata! Ciao dal magico mondo del modding!")
    }

    override fun onRegisterBlocks() {
        // Aggiungiamo un blocco esclusivo tramite le API esposte dal Registry
        val magicBlock = Block("magic_stone", "Pietra Magica")
        
        Registry.registerBlock(modId, magicBlock.id, magicBlock)
        println("[$name] Ho appena aggiunto la Pietra Magica al gioco principale!")
    }
}
