# Draw ASCII — Générateur de dessins ASCII en Scala

Un interpréteur de langage de dessin dans le terminal, écrit en Scala 3 dans un style purement fonctionnel : état immuable, gestion d'erreurs par `Either`, récursivité terminale, et séparation stricte entre la logique pure et les entrées/sorties.

```
AsciiDraw> canvas 12 6
AsciiDraw> setchar x
AsciiDraw> line 11 1 11 4
AsciiDraw> setchar #
AsciiDraw> rect 2 1 6 4
AsciiDraw> fill 3 2 o
AsciiDraw> render
............
..######...x
..#oooo#...x
..#oooo#...x
..######...x
............
```

## Lancement

Prérequis : un JDK (17+) et `sbt`.

```bash
sbt run    # lance le programme en mode interactif
sbt test   # lance les tests unitaires (62 tests)
```

Le programme affiche l'invite `AsciiDraw> ` et attend une commande par ligne. Il se termine avec `quit`, `exit`, ou une fin d'entrée (Ctrl+D).

## Commandes

| Commande | Description |
|---|---|
| `canvas <largeur> <hauteur>` | Crée un canevas vide (dimensions ≥ 1). Doit précéder tout dessin. |
| `point <x> <y>` | Dessine un point (`*` par défaut). |
| `line <x1> <y1> <x2> <y2>` | Dessine une ligne horizontale ou verticale (`#` par défaut). |
| `rect <x> <y> <largeur> <hauteur>` | Dessine le contour d'un rectangle (`#` par défaut). |
| `fill <x> <y> <c>` | Remplit avec `c` la zone connexe des cases portant le même caractère que la case `(x, y)`. |
| `setchar <c>` | Change le caractère utilisé pour tous les dessins suivants. |
| `clear` | Vide le canevas (les dimensions sont conservées). |
| `undo` | Annule la dernière modification du canevas. |
| `redo` | Rétablit la dernière modification annulée. |
| `render` | Affiche le canevas. |
| `quit` / `exit` | Termine le programme. |

Les coordonnées sont indexées à partir de `0` : `(0, 0)` est le coin supérieur gauche, `x` est la colonne, `y` la ligne. Le caractère `.` représente une case vide.

## Exemples d'exécution

### Canevas et point (niveau 1)

```
AsciiDraw> canvas 10 5
AsciiDraw> point 1 1
AsciiDraw> render
..........
.*........
..........
..........
..........
```

### Lignes horizontales et verticales (niveau 2)

```
AsciiDraw> canvas 10 5
AsciiDraw> point 1 1
AsciiDraw> line 2 2 8 2
AsciiDraw> line 4 0 4 4
AsciiDraw> render
....#.....
.*..#.....
..#######.
....#.....
....#.....
```

Une ligne verticale dessinée après une horizontale passe par-dessus à l'intersection. Les diagonales sont refusées :

```
AsciiDraw> line 0 0 3 3
Erreur: seules les lignes horizontales et verticales sont supportées
```

### Rectangles (niveau 3)

```
AsciiDraw> canvas 20 8
AsciiDraw> point 2 3
AsciiDraw> line 1 1 10 1
AsciiDraw> rect 5 2 6 3
AsciiDraw> render
....................
.##########.........
.....######.........
..*..#....#.........
.....######.........
....................
....................
....................
```

### Remplissage, effacement et caractère de dessin (niveau 4)

```
AsciiDraw> canvas 12 6
AsciiDraw> setchar x
AsciiDraw> line 11 1 11 4
AsciiDraw> setchar #
AsciiDraw> rect 2 1 6 4
AsciiDraw> fill 3 2 o
AsciiDraw> render
............
..######...x
..#oooo#...x
..#oooo#...x
..######...x
............
AsciiDraw> clear
AsciiDraw> render
............
............
............
............
............
............
```

`fill` fonctionne comme le « pot de peinture » d'un éditeur d'images : il se propage en 4-connexité sur les cases qui portent le même caractère que la case de départ, et s'arrête aux frontières d'un autre caractère (ici les `#` du rectangle).

### Annulation et rétablissement (bonus)

```
AsciiDraw> canvas 10 5
AsciiDraw> point 1 1
AsciiDraw> rect 3 1 4 2
AsciiDraw> render
..........
.*.####...
...####...
..........
..........
AsciiDraw> undo
AsciiDraw> render
..........
.*........
..........
..........
..........
AsciiDraw> redo
AsciiDraw> render
..........
.*.####...
...####...
..........
..........
```

L'historique est illimité et permet même d'annuler la création du canevas. Annuler au-delà du premier état est une erreur :

```
AsciiDraw> undo
AsciiDraw> undo
AsciiDraw> undo
AsciiDraw> undo
Erreur: rien à annuler
```

À noter : `undo` annule les modifications du canevas, pas les changements de `setchar` ; et tout nouveau dessin après un `undo` invalide le `redo`.

### Gestion des erreurs

Toutes les erreurs sont signalées proprement, sans exception ni arrêt du programme :

```
AsciiDraw> point 2 3
Erreur: aucun canevas n'a été créé
AsciiDraw> canvas 0 4
Erreur: dimensions invalides: 0 4
AsciiDraw> canvas 10 5
AsciiDraw> rect 1 2
Erreur: nombre d'arguments invalide pour rect
AsciiDraw> point 20 3
Erreur: coordonnées (20, 3) en dehors du canevas
AsciiDraw> point a b
Erreur: nombre invalide: a
AsciiDraw> setchar ab
Erreur: caractère invalide: ab
AsciiDraw> circle 1 2
Erreur: commande inconnue: circle
```

## Fonctionnement du programme

### Architecture

```
src/main/scala/asciidraw/
├── Command.scala      # ADT des commandes (sealed trait + case classes)
├── AppError.scala     # ADT de la hiérarchie d'erreurs, avec message pour chacune
├── Canvas.scala       # le modèle : grille immuable et son rendu
├── Session.scala      # l'état complet : canevas + caractère courant + historique
├── Drawing.scala      # géométrie pure : points d'une ligne, d'un rectangle, d'une zone à remplir
├── Parser.scala       # texte -> Either[AppError, Command]
├── Interpreter.scala  # (Session, Command) -> Either[AppError, Result]
└── Main.scala         # boucle REPL : la seule frontière d'entrées/sorties
```

Chaque ligne saisie traverse un pipeline purement fonctionnel :

```
"rect 2 1 6 4"  ──Parser──▶  DrawRect(2, 1, 6, 4)  ──Interpreter──▶  nouvelle Session
      texte                        Command                            (ou AppError)
```

1. **`Parser.parse`** découpe la ligne en tokens (sans expression régulière) et la convertit en `Command` typée. Les entiers sont lus avec `toIntOption` : une entrée mal formée produit une erreur, jamais une exception.
2. **`Interpreter.interpret`** applique la commande à la `Session` courante. Chaque dessin se réduit au même schéma : produire une liste de points (via `Drawing`), vérifier qu'ils sont dans le canevas, puis les écrire avec `foldLeft` dans une **copie** du canevas — l'original n'est jamais modifié.
3. **`Main.loop`** est une boucle récursive terminale (`@tailrec`) qui enchaîne les sessions successives, affiche `Erreur: ...` en cas de `Left`, et le rendu après `render`. C'est le seul fichier qui lit ou écrit sur le terminal.

### Représentation des données

Le canevas est une `case class Canvas(width, height, pixels: Map[(Int, Int), Char])` : seules les cases dessinées sont stockées, les autres sont rendues comme `.`. Dessiner retourne un nouveau `Canvas` (via `copy`), ce qui rend l'historique du bonus trivial : la `Session` empile simplement les états précédents dans `undoStack`/`redoStack` — aucune copie profonde n'est nécessaire puisque tout est immuable et partagé.

Les erreurs forment un ADT (`sealed trait AppError`) : le cœur du programme les propage avec `Either` et des for-comprehensions, et seule la frontière (`Main`) les transforme en message affiché.

### Notions fonctionnelles mises en œuvre

- **Types algébriques** : `Command`, `AppError` (sealed traits + case classes/objects), `Canvas`, `Session`.
- **Pattern matching exhaustif** : dispatch des commandes dans le parseur et l'interpréteur, déconstruction de listes de tokens.
- **Immutabilité totale** : aucun `var`, aucune collection mutable ; l'état avance par copies.
- **Récursivité** : boucle REPL (`Main.loop`) et remplissage par diffusion (`Drawing.explore`), tous deux en récursif terminal.
- **Fonctions d'ordre supérieur** : `map`, `flatMap`, `foldLeft`, `filter`, `find` pour le rendu, le dessin et la validation.
- **Séparation pur / impur** : `Parser`, `Interpreter`, `Drawing`, `Canvas` sont des fonctions pures testées unitairement ; les effets de bord vivent uniquement dans `Main`.

## Tests

```bash
sbt test
```

62 tests unitaires (munit) couvrent les quatre composants purs :

- **`ParserSuite`** — parsing de chaque commande, tolérance aux espaces multiples, commande inconnue, arité invalide, entier ou caractère mal formé.
- **`CanvasSuite`** — création et dimensions invalides, rendu d'une grille vide, limites, dessin d'un ou plusieurs pixels.
- **`DrawingSuite`** — géométrie des lignes (les deux orientations, extrémités inversées, ligne d'un point), contour de rectangle sans doublon, zones de remplissage.
- **`InterpreterSuite`** — tous les cas de la spécification (y compris les deux exemples complets de l'énoncé), chaque cas d'erreur, la sémantique de `setchar`, et le comportement complet de `undo`/`redo`.
