# Guida al Contributo e Sviluppo (CONTRIBUTING)

Grazie per voler partecipare alla crescita della piattaforma KotVox! Essendo questo progetto open source sotto licenza **GNU GPLv3**, accogliamo con calore e incoraggiamo le Pull Request della community!

Per tutelare il cuore pulsante del gioco, chiediamo ad ogni sviluppatore di rispettare queste "Golden Rules".

## 1. Il Core NON si Mofidica Quasi Mai!
A meno che non stiate proponendo un'ottimizzazione critica (esempio: un algoritmo più veloce per la *Greedy Meshing* dei Chunk, o un binding più sicuro per la Keyboard GLFW), il codice contenuto in `engine.core`, `engine.player` e `engine.render` **è in sola lettura**.
Non mandate PR che iniettano direttamente un "Blocco di Legno" nel codice di setup, verrebbero rifiutate istantaneamente.

## 2. Tutto è Modding-First!
Proprio come Hytale, l'approccio ideale a questo Engine è sviluppare funzionalità isolate che ne arricchiscano i contenuti. Se volete aggiungere spade, minerali, NPC, o un nuovo bioma procedurale:
- Fatelo all'interno di una cartella `engine/mods/latuamod/`.
- Assicuratevi che implementi limpidamente l'interfaccia `IMod`.
- Sfruttate le fasi `onInitialize()` e `onRegisterBlocks()`.

## 3. Le Pull Request
Se trovate un bug "motore":
1. Aprire prima una Issue descrittiva con gli step per riprodurre l'errore.
2. Formattare il log di Gradle/Kotlin.
3. Se proponete del codice, inviate la PR scrivendo una descrizione maniacale su cosa risolve, con codice identato e documentato (KDoc style).

## 4. Leggi il README e l'Architettura
Prima di scriverci codice compilalo tu! Cerca nella repo `README.md` i comandi esecutivi, e capisci le differenze tra Stati GUI e GLFW in `Docs/Architecture.md`. 
Buona Programmazione Voxel-Related!
