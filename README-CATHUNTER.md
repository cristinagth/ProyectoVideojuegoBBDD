# Buscagatos / CatHunter

## Resumen

Buscagatos es una version tematizada de Buscaminas. El jugador debe revelar casillas seguras y marcar con ovillos las casillas donde cree que hay gatos ocultos.

El juego se abre desde el menu principal despues de introducir el nombre del jugador.

## Dificultades

| Dificultad | Tablero | Gatos | Tiempo |
| --- | --- | --- | --- |
| Facil | 8 x 8 | 10 | 10 minutos |
| Medio | 16 x 16 | 40 | 20 minutos |
| Dificil | 16 x 30 | 99 | 30 minutos |

La primera casilla revelada y sus alrededores quedan protegidos para evitar perder en el primer clic.

## Temporizador

Cada partida tiene un limite de tiempo segun la dificultad.

- El tiempo se muestra en la barra superior.
- Si llega a `00:00`, la partida termina en derrota.
- Al reiniciar la partida, el tiempo vuelve al limite original de la dificultad.
- El temporizador se detiene al ganar, perder o volver al menu.

## Sistema de eventos

CatHunter incluye eventos aleatorios de rol con tematica gatuna.

Funcionamiento previsto:

- Cada 30 segundos se comprueba si ocurre un evento.
- La probabilidad normal es del 50%.
- Los eventos empiezan despues del primer clic, cuando ya se han colocado los gatos.
- Los eventos se muestran como una banda flotante integrada en el tablero, sin pausar la partida.

Eventos implementados:

| Evento | Tipo | Efecto |
| --- | --- | --- |
| El gato ha pisado un cascabel | Positivo | Coloca una bandera correcta sobre un gato oculto. |
| Has encontrado un ovillo brillante | Positivo | Suma 15, 30 o 45 segundos. |
| Un gato tira un vaso al suelo | Negativo | Resta 15, 30 o 45 segundos. |
| Un ovillo rueda por el tablero | Negativo | Quita una bandera aleatoria. |
| Un maullido lejano te guia | Positivo | Revela una casilla segura oculta. |
| El gato se duerme al sol | Positivo | Pausa el temporizador durante 10 segundos. |
| Un gato curioso empuja un ovillo mal colocado | Positivo | Quita una bandera incorrecta. |
| Un gato escurridizo cambia de escondite | Negativo | Mueve un gato oculto a otra casilla oculta y recalcula los numeros. |
| Evento narrativo | Neutro | Muestra ambientacion sin alterar la partida. |

## Archivos principales

- `CatHunterIntro.java`: pantalla de introduccion, ambiente y selector de dificultad.
- `CatHunterBoard.java`: tablero, temporizador, eventos, dibujo y reglas principales.
- `Cell.java`: estado de cada casilla.
