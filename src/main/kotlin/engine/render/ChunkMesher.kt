package engine.render

import engine.world.Chunk
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

/**
 * Il Mesher trasforma i dati grezzi del Chunk (gli array di numeri)
 * in vertici geometrici (triangoli) che la GPU può effettivamente disegnare.
 * Implementa un algoritmo fondamentale nei motori voxel: il Face Culling.
 */
object ChunkMesher {

    /**
     * Genera la mesh per un chunk.
     * @return Un FloatBuffer "nativo" (fuori dalla heap di Java) contenente le coordinate spaziali (x,y,z) 
     *         e i colori (r,g,b) dei vertici da disegnare.
     */
    fun generateMesh(chunk: Chunk): FloatBuffer {
        // Usiamo una lista temporanea per accumulare i vertici prima di inviarli alla GPU
        val vertices = mutableListOf<Float>()

        // Iteriamo su tutti i blocchi del chunk (16x16x16 = 4096 blocchi)
        for (x in 0 until Chunk.SIZE) {
            for (y in 0 until Chunk.SIZE) {
                for (z in 0 until Chunk.SIZE) {
                    val blockId = chunk.getBlock(x, y, z)
                    if (blockId == 0.toShort()) continue // Salta l'aria, non ha senso disegnarla!

                    // --- FACE CULLING ---
                    // Un cubo ha 6 facce, ma in un mondo fatto a blocchi la maggior parte delle facce 
                    // è nascosta da altri blocchi adiacenti (es: i blocchi sottoterra non si vedono).
                    // Disegnare facce invisibili distruggerebbe le prestazioni della GPU.
                    // Quindi: controlliamo ogni direzione adiacente. Se c'è aria, la faccia è esposta
                    // e dobbiamo disegnarla. Se c'è un altro blocco solido, la saltiamo!
                    
                    // Faccia Dietro (Back: asse Z negativo)
                    if (chunk.isAir(x, y, z - 1)) addFace(vertices, x, y, z, 0)
                    // Faccia Davanti (Front: asse Z positivo)
                    if (chunk.isAir(x, y, z + 1)) addFace(vertices, x, y, z, 1)
                    // Faccia Sinistra (Left: asse X negativo)
                    if (chunk.isAir(x - 1, y, z)) addFace(vertices, x, y, z, 2)
                    // Faccia Destra (Right: asse X positivo)
                    if (chunk.isAir(x + 1, y, z)) addFace(vertices, x, y, z, 3)
                    // Faccia Sotto (Bottom: asse Y negativo)
                    if (chunk.isAir(x, y - 1, z)) addFace(vertices, x, y, z, 4)
                    // Faccia Sopra (Top: asse Y positivo)
                    if (chunk.isAir(x, y + 1, z)) addFace(vertices, x, y, z, 5)
                }
            }
        }

        // Allocazione OFF-HEAP (memoria nativa gestita esternamente alla JVM).
        // Il Garbage Collector (GC) di Java/Kotlin sarebbe sovraccaricato se dovesse pulire
        // costantemente array di milioni di float generati ad ogni aggiornamento dei chunk.
        // Utilizzando MemoryUtil.memAllocFloat (una funzione di LWJGL che chiama malloc in C sotto il cofano),
        // aggiriamo il GC. ATTENZIONE: Questa memoria VA DEALLOCATA MANUALMENTE (MemoryUtil.memFree) 
        // una volta caricata sulla GPU, altrimenti avremo memory leak!
        val buffer = MemoryUtil.memAllocFloat(vertices.size)
        // Copiamo i dati dalla lista (Java Heap) al buffer nativo (Off-Heap)
        vertices.forEach { buffer.put(it) }
        buffer.flip() // Preparalo per la lettura da parte di OpenGL
        
        return buffer
    }

    private fun addFace(vertices: MutableList<Float>, x: Int, y: Int, z: Int, face: Int) {
        val xf = x.toFloat()
        val yf = y.toFloat()
        val zf = z.toFloat()

        when (face) {
            0 -> { // BACK
                addVertex(vertices, xf, yf, zf, 0.5f, 0.5f, 0.5f)
                addVertex(vertices, xf + 1, yf, zf, 0.5f, 0.5f, 0.5f)
                addVertex(vertices, xf + 1, yf + 1, zf, 0.5f, 0.5f, 0.5f)
                addVertex(vertices, xf + 1, yf + 1, zf, 0.5f, 0.5f, 0.5f)
                addVertex(vertices, xf, yf + 1, zf, 0.5f, 0.5f, 0.5f)
                addVertex(vertices, xf, yf, zf, 0.5f, 0.5f, 0.5f)
            }
            1 -> { // FRONT
                addVertex(vertices, xf, yf, zf + 1, 0.2f, 0.8f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf + 1, 0.2f, 0.8f, 0.2f)
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.2f, 0.8f, 0.2f)
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.2f, 0.8f, 0.2f)
                addVertex(vertices, xf, yf + 1, zf + 1, 0.2f, 0.8f, 0.2f)
                addVertex(vertices, xf, yf, zf + 1, 0.2f, 0.8f, 0.2f)
            }
            2 -> { // LEFT
                addVertex(vertices, xf, yf + 1, zf + 1, 0.3f, 0.3f, 0.7f)
                addVertex(vertices, xf, yf + 1, zf, 0.3f, 0.3f, 0.7f)
                addVertex(vertices, xf, yf, zf, 0.3f, 0.3f, 0.7f)
                addVertex(vertices, xf, yf, zf, 0.3f, 0.3f, 0.7f)
                addVertex(vertices, xf, yf, zf + 1, 0.3f, 0.3f, 0.7f)
                addVertex(vertices, xf, yf + 1, zf + 1, 0.3f, 0.3f, 0.7f)
            }
            3 -> { // RIGHT
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.8f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf + 1, zf, 0.8f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf, 0.8f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf, 0.8f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf + 1, 0.8f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.8f, 0.2f, 0.2f)
            }
            4 -> { // BOTTOM
                addVertex(vertices, xf, yf, zf, 0.2f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf, 0.2f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf + 1, 0.2f, 0.2f, 0.2f)
                addVertex(vertices, xf + 1, yf, zf + 1, 0.2f, 0.2f, 0.2f)
                addVertex(vertices, xf, yf, zf + 1, 0.2f, 0.2f, 0.2f)
                addVertex(vertices, xf, yf, zf, 0.2f, 0.2f, 0.2f)
            }
            5 -> { // TOP
                addVertex(vertices, xf, yf + 1, zf, 0.9f, 0.9f, 0.1f)
                addVertex(vertices, xf + 1, yf + 1, zf, 0.9f, 0.9f, 0.1f)
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.9f, 0.9f, 0.1f)
                addVertex(vertices, xf + 1, yf + 1, zf + 1, 0.9f, 0.9f, 0.1f)
                addVertex(vertices, xf, yf + 1, zf + 1, 0.9f, 0.9f, 0.1f)
                addVertex(vertices, xf, yf + 1, zf, 0.9f, 0.9f, 0.1f)
            }
        }
    }

    private fun addVertex(vertices: MutableList<Float>, x: Float, y: Float, z: Float, r: Float, g: Float, b: Float) {
        vertices.add(x)
        vertices.add(y)
        vertices.add(z)
        vertices.add(r)
        vertices.add(g)
        vertices.add(b)
    }
}
