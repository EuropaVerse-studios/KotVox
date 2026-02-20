# KotVox

Un potente motore di gioco Voxel in Kotlin e OpenGL (LWJGL3) progettato fin dall'inizio con un'architettura **Modding-First**, ispirata profondamente dalle dinamiche e necessità tecniche di colossi come Hytale o Minecraft.

## ⭐ Features

- **Rendering Voxel Ottimizzato**: Implementato algoritmo di _Face Culling_ e memory allocation "Off-Heap" per mantenere stabili le prestazioni al caricamento dei chunk, aggirando i colli di bottiglia del Garbage Collector della JVM.
- **Architettura Modulare e Disaccoppiata**: Il Core principale del gioco vive indipendentemente da eventuali script esterni.
- **UI Integrata (ImGui)**: Menù interattivi e listbox di ultima generazione iniettate in-game.
- **Fisica Gravitazionale**: Sistema di movimento calcolato matematicamente assecondato alla topologia generata proceduralmente per il terreno.

## 🚀 Come avviare il Motore

Ti basterà possedere una JDK relativamente moderna installata sul tuo sistema (suggerita la **JDK 17 o 21**). Grazie al Gradle Wrapper, l'uso è immediato.

```bash
# Sotto ambiente Windows, apri il terminale del progetto:
./gradlew run
```

Per chiuderlo, usa l'`ESC` della tastiera per visualizzare il Main Menu e clicca **Esci**.

## 📖 Documentazione

Vuoi comprendere come funziona il gioco, come muovere i tuoi primi passi nello scriverne espansioni o studiare le nostre scelte architetturali? Visita l'apposita directory inclusa in questo archivio: `Docs/`.

- [`Docs/Architecture.md`](Docs/Architecture.md) - Come il motore è strutturato e come lavora il ciclo di OpenGL.
- [`Docs/ModdingAPI.md`](Docs/ModdingAPI.md) - Istruzioni per sviluppare Mod esterne senza toccare il Codice Core.
- [`Docs/Contributing.md`](Docs/Contributing.md) - Linee guida ferree su cosa ci aspettiamo dalle tue Pull Request.

---
**Nota Legale:** Questo progetto è rilasciato sotto la licenza open source **GNU GPLv3** (vedi il file `LICENSE`). Sei libero di studiare, modificare e perfino distribuire il codice, garantendo allo stesso tempo le medesime libertà agli utenti delle tue varianti modificate!
