# Architettura di KotVox

L'engine è strutturato in pacchetti ben definiti che limitano l'accoppiamento tra il codice base, la logica di calcolo del mondo visibile, e le azioni dell'Utente.

## Struttura della Source (`src/main/kotlin/engine/`)

Le cartelle principali attorno al quale lo scheletro è costruito sono le seguenti:

### 1. `core`
Qui giace il battito vitale del gioco.
- **`KotVox.kt`**: È la classe **Entry Point**. Inizializza OpenGL tramite GLFW, gestisce il loop primario `while (!windowShouldClose())` calcolando il Delta Time per le animazioni o la fisica, e instrada i controlli in un sistema a stati.
- **`GameState.kt`**: Definisce e divide le responsabilità dei device di input: quando il cursore è gestito dalla GUI ImGui (`MAIN_MENU`), la Telecamera non risponde a spostamenti diretti.

### 2. `render`
Tutto ciò che deve essere sputato a schermo abita in questo layer. A sua volta questo layer si sdoppia:
- **`Shader.kt`** e **`ChunkMesher`**: Codice di bassissimo livello per dialogare con la scheda video C++. `ChunkMesher` ottimizza ed elide (Face Culling) migliaia di poligoni non a vista che gravano la RAM e VRAM.
- **`GuiManager.kt`**: Poggia le primitive fornite da `imgui-java`. Renderizza con stile "Windowed" finestre, textbox ed header senza pesare sul render tree del mondo Voxel.

### 3. `player`
Il pacchetto relativo all'Input spaziale del Player (`Camera`).
- La classe `Player.kt`, al contrario di altri sistemi, fonde le logiche della Visione visiva a quelle prettamente topografiche: per ogni step, converte l'angolazione (Yaw, Pitch) in vettori seno-coseno, interrogando il `World` procedurale (tramite `world.getGroundHeight()`) per allineare l'altezza Y ai piedi del personaggio con la cima dell'erba.

### 4. `world` e `world.gen`
Il contenitore in-memory della mappa.
- **Chunk array monodimensionali**: In `Chunk.kt` un cubo tridimensionale 16x16x16 è schiacciato su indicizzazione locale `ly * SIZE * SIZE + lz * SIZE + lx`. Questo abbassa mostruosamente il carico sull'Heap per Chunk immensi.
- **`TerrainGenerator.kt`**: Restituisce valori pseudocasuali usando la matematica del rumore (Seno), su cui basa l'idratazione verticale ed iniezione in blocco di erba o terra o pietra nei Chunk grezzi.

### 5. `modding` & `registry`
KotVox è un sistema cieco. Non ha idea di cosa sta renderizzando: legge dei **ID Stringa** in memoria e li trasforma in solidi.
Si appoggia a una HashMap centrale (`Registry.kt`), e ad un orchestratore isolato (`ModManager.kt`) che fa da Proxy/Hooking. Nessuna Modde ha il permesso di alterare i 4 precedenti pacchetti. Può solo aggiungere chiavi al Registry e ascoltarne i trigger.
