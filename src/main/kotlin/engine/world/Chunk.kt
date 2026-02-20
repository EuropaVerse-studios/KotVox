package engine.world

/**
 * Un Chunk è un blocco di mondo di dimensioni fisse (16x16x16).
 * Questo permette di caricare e scaricare parti del mondo in modo efficiente.
 * Invece di gestire ogni singolo blocco come un oggetto, gestiamo interi Chunk,
 * riducendo drasticamente l'uso della memoria e migliorando le prestazioni.
 */
class Chunk(val x: Int, val y: Int, val z: Int) {
    companion object {
        const val SIZE = 16
    }

    // Array 3D convertito in 1D (piatto) che contiene gli ID dei blocchi. 
    // Usiamo ShortArray invece di Array<Int> o simili per risparmiare memoria 
    // (possono esserci migliaia di chunk contemporaneamente in memoria!).
    // Un Int occupa 4 byte, uno Short 2 byte. In un chunk ci sono 4096 blocchi,
    // quindi risparmiamo 8KB a chunk.
    private val blocks = ShortArray(SIZE * SIZE * SIZE)

    /**
     * Imposta un blocco in una posizione specifica del chunk (coordinate locali da 0 a 15).
     * @param lx Y locale (0-15)
     * @param ly Y locale (0-15)
     * @param lz Z locale (0-15)
     * @param blockId L'ID del blocco (es. 0 per aria, 1 per pietra)
     */
    fun setBlock(lx: Int, ly: Int, lz: Int, blockId: Short) {
        if (lx in 0 until SIZE && ly in 0 until SIZE && lz in 0 until SIZE) {
            blocks[getIndex(lx, ly, lz)] = blockId
        }
    }

    /**
     * Ritorna l'ID del blocco alle coordinate locali specificate.
     */
    fun getBlock(lx: Int, ly: Int, lz: Int): Short {
        return if (lx in 0 until SIZE && ly in 0 until SIZE && lz in 0 until SIZE) {
            blocks[getIndex(lx, ly, lz)]
        } else 0
    }

    /**
     * Controlla se il blocco alle coordinate specificate è aria (comprese le coordinate fuori dal chunk).
     * Molto utile per il ChunkMesher, per sapere se deve disegnare una faccia che tocca il bordo.
     */
    fun isAir(lx: Int, ly: Int, lz: Int): Boolean {
        // Se le coordinate escono dal chunk, consideriamo che ci sia aria (temporaneo, 
        // finché non c'è il controllo con i chunk adiacenti).
        if (lx !in 0 until SIZE || ly !in 0 until SIZE || lz !in 0 until SIZE) return true
        return getBlock(lx, ly, lz) == 0.toShort()
    }

    // Trasforma le coordinate 3D in un singolo indice 1D per l'array.
    // L'accesso ad un array 1D è estremamente più veloce e continuo nella memoria 
    // rispetto a un array 3D come Array<Array<Array<Short>>>.
    // La formula è: indice = x + (y * larghezza) + (z * larghezza * altezza).
    private fun getIndex(x: Int, y: Int, z: Int) = x + (y * SIZE) + (z * SIZE * SIZE)
}
