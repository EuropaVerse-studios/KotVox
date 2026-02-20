package engine.registry

/**
 * Il Registro è il cuore del sistema "modding-first".
 * Invece di avere i blocchi scritti direttamente nel codice del mondo,
 * li salviamo qui dentro. I mod non faranno altro che aggiungere oggetti a questo registro.
 */
object Registry {
    // Usiamo una HashMap per salvare i blocchi con la loro "chiave" (es. "engine:grass")
    private val blocks = HashMap<String, Block>()

    /**
     * Registra un nuovo blocco. 
     * @param namespace Di solito il nome del mod (es. "base", "mymod")
     * @param id Il nome del blocco (es. "dirt")
     * @param block L'istanza del blocco
     */
    fun registerBlock(namespace: String, id: String, block: Block) {
        val key = "$namespace:$id"
        if (blocks.containsKey(key)) {
            println("ATTENZIONE: Il blocco $key è già registrato e verrà sovrascritto!")
        }
        blocks[key] = block
        println("Registrato blocco: $key")
    }

    /**
     * Recupera un blocco tramite il suo ID completo.
     */
    fun getBlock(key: String): Block? = blocks[key]

    /**
     * Stampa tutti i blocchi registrati (utile per debug).
     */
    fun listBlocks() {
        println("--- Blocchi Registrati ---")
        blocks.forEach { (k, v) -> println("$k -> ${v.name}") }
    }
}
