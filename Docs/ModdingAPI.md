# Modding API: Creare la tua Prima Espansione

KotVox prevede sin dal primo giorno che la Community non debba lottare con il codice sorgente (spesso criptico) relativo a OpenGL. Il nostro ModManager risolve questo!

## L'interfaccia `IMod`
Le Mod sono identificate a runtime (simulato) caricando tutte le classi che estendono o implementano `engine.modding.IMod`. 

La tua mod *deve obbligatoriamente* possedere un Costruttore vuoto, ed effettuare l'override di due principali campi stringa e due metodi fase.

```kotlin
class MyCustomMod : IMod {
    override val modId = "my_custom_mod"
    override val name = "La Mia Prima Mod"
    override val version = "0.0.1-Alpha"

    // FASE 1: Inizializzazione Pre-Engine
    override fun onInitialize() {
        println("Preparando database e configurazioni custom!")
    }

    // FASE 2: Injecting nel Sistema Centrale
    override fun onRegisterBlocks() {
        // Qui hai il controllo totale sui Registri!
        println("Registro i miei super blocchi!")
    }
}
```

## Come Aggiungere Materiali e Blocchi al Mondo
Per dare vita effettiva al gioco, sfrutta `engine.registry.Registry`.

All'interno di `onRegisterBlocks()`, inietta le tue instanze usando chiavi Univoche: in questo modo il gioco differenzierà senza problemi:
- `engine:dirt` -> Terra di KotVox Standard
- `my_custom_mod:red_dirt` -> Tua variante di mod

### Esempio Pratico: Creare un Minerale di Zaffiro

```kotlin
override fun onRegisterBlocks() {
    // Il nome "sapphire" è l'ID, "Zaffiro Lucente" è il nome tradotto.
    val mySapphire = Block("sapphire", "Zaffiro Lucente")
    
    // Inseriscilo dentro la HashMap universale
    Registry.registerBlock(modId, mySapphire.id, mySapphire)
}
```
*Da questo momento, ogni Chunk generato o caricato potrà posizionare nella sua matrice `[Chunk.SIZE]` blocchi corrispondenti all'ID dello Zaffiro!*

## Visualizzare in-game i tuoi risultati
Grazie a `GuiManager` e la nostra implementazione `ImGui`, l'Utente che scaricherà da internet la classe della tua Mod, la vedrà comparire magicamente sotto la voce **MainMenu -> Gestione Mods**! E tu non hai dovuto toccare nessuna riga al di fuori della tua cartella `engine.mods.my_custom_mod`!
