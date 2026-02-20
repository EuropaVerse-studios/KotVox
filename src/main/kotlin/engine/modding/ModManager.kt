package engine.modding

// Nelle versioni future più complesse, useremo ClassGraph o Reflections 
// per cercare nei JAR tutte le classi che implementano IMod. 
// Per ora, e per evitare di installare troppe dipendenze, simuliamo la pipeline 
// creando noi un caricatore "Dinamico-simulato".

/**
 * Gestisce l'intero ciclo di vita delle mod. 
 * È completamente indipendente dal motore di base. Il gioco potrebbe non avere 
 * alcuna mod e lui non andrebbe in blocco.
 */
object ModManager {
    private val loadedMods = mutableListOf<IMod>()

    /**
     * Cerca, istanzia e salva tutte le mod in memoria.
     * Questa funzione va chiamata per PRIMA COSA nell'avvio del gioco.
     */
    fun loadMods() {
        println("[ModManager] Scansione delle mod in corso...")
        
        // --- SIMULAZIONE DI DISCOVERY DINAMICA (Reflections) ---
        // Se usassimo java.util.ServiceLoader o una vera Reflection avremmo qualcosa come:
        // val reflections = Reflections("engine.mods")
        // val modClasses = reflections.getSubTypesOf(IMod::class.java)
        // ecc ecc...
        try {
            // Proviamo a caricare programmaticamente la nostra mod di test, simulando il loader
            val exampleModClass = Class.forName("engine.mods.examplemod.ExampleMod")
            val modInstance = exampleModClass.getDeclaredConstructor().newInstance() as IMod
            loadedMods.add(modInstance)
        } catch (e: Exception) {
            println("[ModManager] Nessuna mod dinamica rilevata (o errore nel caricamento).")
        }
        
        // Fase 1: Notifica a tutte le mod che sono state caricate.
        for (mod in loadedMods) {
            println("[ModManager] Caricata mod: ${mod.name} (${mod.version}) - ID: ${mod.modId}")
            mod.onInitialize()
        }
        
        println("[ModManager] Trovate ${loadedMods.size} mod.")
    }

    /**
     * Richiama la fase in cui le mod inseriscono i loro oggetti e blocchi
     * all'interno dei registri unificati del gioco.
     * Va chiamato DOPO aver inserito i blocchi base del Core.
     */
    fun initModRegistries() {
        for (mod in loadedMods) {
            println("[ModManager] Registrazione blocchi da parte di: ${mod.modId}...")
            mod.onRegisterBlocks()
        }
    }

    /**
     * Ritorna la lista in sola lettura delle mod caricate dal manager.
     */
    fun getLoadedMods(): List<IMod> {
        return loadedMods.toList()
    }
}
