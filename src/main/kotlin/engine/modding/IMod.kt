package engine.modding

/**
 * L'interfaccia base che ogni Mod deve implementare per essere caricata dal motore.
 * Il ModManager chiamerà i metodi in quest'ordine durante la sequenza di avvio.
 */
interface IMod {
    /**
     * L'ID univoco della mod (es. "examplemod"). Serve per il Registry e i file.
     */
    val modId: String

    /**
     * Il nome leggibile della mod.
     */
    val name: String

    /**
     * La versione della mod.
     */
    val version: String

    /**
     * Fase 1: Inizializzazione principale della mod. Viene chiamata per prima,
     * subito dopo la scoperta delle mod. Ottimo posto per preparare configurazioni.
     */
    fun onInitialize()

    /**
     * Fase 2: Registrazione dei blocchi base. Chiamata quando il Registry è pronto
     * ad accogliere nuovi blocchi e dopo che i blocchi "core" sono stati caricati.
     */
    fun onRegisterBlocks()
}
