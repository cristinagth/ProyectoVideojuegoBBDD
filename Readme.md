# ♟️ Proyecto Juego de Ajedrez en Java ♟️

Proyecto académico en **Java** que implementa un tablero de ajedrez con **interfaz gráfica (Swing)**. El proyecto está en evolución (intención de implementar mas medidas complicadas en proyecto de ingeniería 2): actualmente permite jugar **dos jugadores en local** con selección por clic, **resaltado de movimientos legales**, capturas, **control de turnos** y detección de **jaque / jaque mate** (con opción de reinicio). 

---

## Descripción

Actualmente, el proyecto muestra un tablero de ajedrez utilizando BorderLayout en una ventana Swing. Las piezas se seleccionan con clic y el tablero **resalta en verde** las casillas a las que la pieza puede moverse según sus reglas actuales.

Funcionalidades ya incorporadas en la versión actual:

- **Tablero 8×8** renderizado manualmente (JPanel), con **coordenadas** (A–H / 1–8) alrededor del tablero.
- **Piezas completas** (peón, torre, caballo, alfil, reina y rey) con imágenes cargadas desde `figures/`.
- **Control de turnos** (empiezan blancas) y validación básica para evitar mover piezas fuera de turno.
- **Capturas**: al mover a una casilla ocupada por rival, la pieza capturada se elimina.
- **Jaque**: tras cada movimiento se comprueba si el rey del jugador que debe mover queda atacado y se muestra un aviso.
- **Jaque mate**: se simulan movimientos para determinar si el jugador en jaque tiene salvación; si no, se muestra un diálogo para reiniciar.

> Nota: todavía hay reglas oficiales no implementadas (enroque, en passant, coronación, validación completa de “no dejar tu rey en jaque”, empates, etc.).
---

## Necesidades del Proyecto

- Ofrecer una experiencia interactiva y educativa sobre el ajedrez.
- Permitir a los usuarios jugar partidas completas, practicar movimientos y aprender.
- Facilitar la ampliación y mejora continua del software.

---

## Requisitos Funcionales

-  **Visualización de un tablero de ajedrez de 8x8 casillas.**
-  **Implementación de todas las piezas y sus movimientos legales (base).**  
  Incluye: peón (avance 1/2 + capturas diagonales), torre, alfil, reina, caballo y rey (1 casilla). 
-  **Validación de reglas oficiales de ajedrez (jaque, jaque mate, empate, etc.).**  
  Implementado: **jaque** + **jaque mate** (detección y diálogo de reinicio). Pendiente: empates/tablas y validación completa de movimientos que dejan al rey en jaque en todos los casos. 
-  **Interfaz gráfica intuitiva y centrada.**  
  Selección por clic y **resaltado** de casillas disponibles.
-  **Posibilidad de jugar partidas entre dos jugadores (local).** 
-  **Registro y visualización de movimientos realizados.** (pendiente)
-  **Opción de reiniciar la partida y guardar/cargar el estado del juego.**  
  Reinicio: disponible al finalizar por jaque mate (y reinicio interno del tablero). Guardar/cargar: pendiente.
-  **Animaciones y efectos visuales mejorados.**  
  Implementado: resaltado de movimientos + coordenadas.

---

## Requisitos No Funcionales

- El sistema debe ser fácil de usar y accesible para usuarios de todos los niveles.
- El código debe ser modular, mantenible y documentado (clase base `Chesspiece` + piezas especializadas).
- El rendimiento debe ser óptimo, permitiendo una experiencia fluida.
- Compatibilidad con diferentes sistemas operativos y entornos de desarrollo.

---

## Objetivos futuros

- Implementación de IA para jugar contra el ordenador.
- Modo online para partidas remotas.
- Estadísticas y análisis de partidas.
- Reglas oficiales pendientes: **enroque**, **en passant**, **coronación**, validación completa de “no quedar en jaque”, y **tablas** (ahogado, repetición, 50 movimientos, etc.).
- Registro de movimientos y opción de guardar/cargar partida.
- Animaciones y mejoras visuales adicionales.

---

## Estructura del Proyecto

```
ProyectoVideojuego/
└── ProyectoVideojuego/
├── Main.java
├── Board.java
├── Chesspiece.java
├── Pawn.java
├── Rook.java
├── Knight.java
├── Bishop.java
├── Queen.java
├── King.java
├── figures/
│ ├── pawn-white.png / pawn-black.png
│ ├── rook-white.png / rook-black.png
│ ├── knight-white.png / knight-black.png
│ ├── bishop-white.png / bishop-black.png
│ ├── queen-white.png / queen-black.png
│ └── king-white.png / king-black.png
└── Readme.md
```

> Importante: las imágenes se cargan desde `figures/`, así que la carpeta debe estar disponible en el **classpath** al ejecutar la aplicación.

---

## ¿Cómo ejecutar?

**Windows (PowerShell / CMD):**
1. Desde la carpeta raíz del proyecto (`ProyectoVideojuegoBBDD`):
   - `javac -d . *.java`
2. Ejecuta:
   - `java ProyectoVideojuegoBBDD.Main`
3. Si estás situado una carpeta por encima del proyecto, por ejemplo en `D:\ProyectoBBDD`, usa:
   - `javac -d . ProyectoVideojuegoBBDD\*.java`
   - `java ProyectoVideojuegoBBDD.Main`
4. ¡Disfruta de tu partida de ajedrez!

**macOS / Linux:**
1. Desde la carpeta raíz del proyecto:
   - `javac -d . *.java`
2. Ejecuta:
   - `java ProyectoVideojuegoBBDD.Main`
3. ¡Disfruta de tu partida de ajedrez!

---

## Créditos

Desarrollado por Cristina Garcia Andreeva, Pedro Manuel Ribeiro Andrade y Patricia Onaney Reyes Sanchez.

Inspirado en el aprendizaje de Java y la programación de videojuegos.

---

¡Gracias por visitar este proyecto! Siéntete libre de contribuir, sugerir mejoras o usarlo como base para tus propios experimentos.