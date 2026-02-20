package engine.world.gen

import engine.world.Chunk
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generatore procedurale del mondo a chunk.
 */
object TerrainGenerator {

    /**
     * Calcola l'altezza locale a livello globale usando funzioni matematiche semplici.
     */
    fun getHeightAt(globalX: Int, globalZ: Int): Int {
        val heightNoise = (sin(globalX / 8.0) * 3 + cos(globalZ / 8.0) * 3).toInt()
        return 5 + heightNoise // 5 è l'altezza base della mappa
    }

    /**
     * Genera un singolo chunk popolandolo con blocchi in base all'altezza.
     */
    fun generateChunk(cx: Int, cz: Int): Chunk {
        val chunk = Chunk(cx, 0, cz)

        for (x in 0 until Chunk.SIZE) {
            for (z in 0 until Chunk.SIZE) {
                // Coordinate assolute del blocco nell'immenso mondo voxel
                val globalX = cx * Chunk.SIZE + x
                val globalZ = cz * Chunk.SIZE + z

                val h = getHeightAt(globalX, globalZ)

                for (y in 0 until Chunk.SIZE) {
                    if (y < h - 1) {
                        chunk.setBlock(x, y, z, 1) // Pietra (ID = 1)
                    } else if (y == h - 1) {
                        chunk.setBlock(x, y, z, 2) // Erba (ID = 2)
                    }
                }
            }
        }
        return chunk
    }
}
