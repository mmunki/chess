# The levels of the computer

Every level is a search with a depth and a rate of mistakes.  The mistake is
not a wild move: the moves of the position are put in the order of a short
search and one of the good ones is taken.

| Level | Depth | Time | Mistakes | Rating |
| --- | --- | --- | --- | --- |
| Beginner | 1 | 0.2 s | 75 % | 400 |
| Easy | 2 | 0.3 s | 50 % | 800 |
| Medium | 3 | 0.5 s | 30 % | 1200 |
| Hard | 5 | 1.2 s | 15 % | 1600 |
| Expert | 6 | 3 s | 5 % | 1750 |
| Master | 7 | 6 s | 0 % | 1900 |

## How the numbers were measured

The levels play against each other, every level against the level above it,
the colours are changed every game.  The score of the weaker level gives the
distance between the two:

```
difference = -400 * log10(1 / score - 1)
```

The measurement is done by `com.mmunki.chess.test.Calibrate`:

```bash
java -cp build/classes:build/test-classes com.mmunki.chess.test.Calibrate 20
```

It prints the score and the distance of every pair.

The measurement of six games per pair says this:

| Pair | Score of the weaker level | Distance |
| --- | --- | --- |
| Beginner against Easy | 0.00 | at least 400 |
| Easy against Medium | 0.00 | at least 400 |
| Medium against Hard | 0.00 | at least 400 |
| Hard against Expert | 0.33 | about 120 |

The rate of mistakes is what makes the levels apart: a level that hangs a
piece every second time wins every game against the level below it, so the
distance between two levels is at least 400 rating points.  The levels at the
top are much closer to each other, there the depth is what counts.

The table takes the distances of the measurement: 400 points between the
levels at the bottom and about 150 points between the levels at the top.

The absolute numbers are an estimate.  The measurement is made against the
levels of this game and not against the players of a chess server, so the
numbers can be off by a hundred points or so.  What they say is the order of
the levels and the distance between them, and that is what the rating test
needs.
