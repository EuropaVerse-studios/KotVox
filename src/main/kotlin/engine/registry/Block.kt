package engine.registry

/**
 * Questa classe rappresenta un Blocco generico nel gioco.
 * Tutte le proprietà di un blocco (es. "erba", "pietra") sono definite qui.
 */
open class Block(
    val id: String,          // Es: "grass"
    val name: String,        // Es: "Blocco d'Erba"
    val isTransparent: Boolean = false
) {
    // In futuro qui aggiungeremo proprietà come:
    // - Texture coordinates
    // - Hardness (resistenza)
    // - Sound type
    
    override fun toString(): String = "Block(id='$id', name='$name')"
}
