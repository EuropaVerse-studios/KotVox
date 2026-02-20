package engine.world

import engine.world.gen.TerrainGenerator

/**
 * Gestisce l'intera mappa globale caricata in memoria.
 */
class World {
    // Mappa che associa coordinate (X, Z) al Chunk corrispondente
    private val chunks = mutableMapOf<Pair<Int, Int>, Chunk>()

    /**
     * Carica o genera i chunk iniziali (5x5 intorno all'origine).
     * @return Una lista di chunk da renderizzare.
     */
    fun initStartMap(): List<Chunk> {
        for (cx in -2..2) {
            for (cz in -2..2) {
                val chunk = TerrainGenerator.generateChunk(cx, cz)
                chunks[Pair(cx, cz)] = chunk
            }
        }
        return chunks.values.toList()
    }

    /**
     * Ottiene l'ID del blocco a specifiche coordinate assolute nel mondo (globali).
     */
    fun getBlockAt(globalX: Float, globalY: Float, globalZ: Float): Short {
        // Calcola il chunk in cui ci troviamo (dividendo per 16 arrotondato verso il basso)
        var cx = globalX.toInt() / Chunk.SIZE
        if (globalX < 0 && globalX % Chunk.SIZE != 0f) cx--
        
        var cz = globalZ.toInt() / Chunk.SIZE
        if (globalZ < 0 && globalZ % Chunk.SIZE != 0f) cz--

        val chunk = chunks[Pair(cx, cz)] ?: return 0 // Se il chunk non esiste, c'è aria

        // Calcola la posizione locale nel chunk
        var localX = globalX.toInt() % Chunk.SIZE
        if (localX < 0) localX += Chunk.SIZE
        
        var localZ = globalZ.toInt() % Chunk.SIZE
        if (localZ < 0) localZ += Chunk.SIZE
        
        val localY = globalY.toInt()

        // Controlla il limite verticale
        if (localY < 0 || localY >= Chunk.SIZE) return 0

        return chunk.getBlock(localX, localY, localZ)
    }

    /**
     * Ritorna l'altezza in Y del terreno solido più alto sotto le coordinate specificate.
     * Molto utile per capire dove "far atterrare" il giocatore.
     */
    fun getGroundHeight(globalX: Float, globalZ: Float): Int {
        // Interroghiamo direttamente il generatore per le altezze del chunk
        // visto che per adesso il mondo non viene modificato/scavato.
        // Se il gioco supporterà la rottura di blocchi, bisognerebbe scorrere l'asse Y 
        // dall'alto verso il basso partendo da 15 finché getBlockAt() != 0
        return TerrainGenerator.getHeightAt(globalX.toInt(), globalZ.toInt())
    }
}
