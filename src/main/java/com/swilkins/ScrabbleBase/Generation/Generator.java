package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.Coordinates;
import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardSquare;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidBoardStateException;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidRackLengthException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRackCapacityException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRootException;
import com.swilkins.ScrabbleBase.Vocabulary.Alphabet;
import com.swilkins.ScrabbleBase.Vocabulary.Trie;
import com.swilkins.ScrabbleBase.Vocabulary.TrieNode;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Contains logic for exhaustive move generation
 * given game context.
 */
public class Generator {

  private static class ValidationResult {

    private final int dimensions;
    private final int existingTileCount;

    public ValidationResult(int dimensions, int existingTileCount) {
      this.dimensions = dimensions;
      this.existingTileCount = existingTileCount;
    }

  }

  private static class CrossedTilePlacement {

    private final TilePlacement root;
    private final Set<TilePlacement> cross;

    public CrossedTilePlacement(TilePlacement root, Set<TilePlacement> cross) {
      this.root = root;
      this.cross = cross;
    }

    public TilePlacement getRoot() {
      return root;
    }

    public Set<TilePlacement> getCross() {
      return cross;
    }

  }

  private static TrieNode root = null;
  private static Integer rackCapacity = null;

  public static void setRoot(TrieNode root) {
    Generator.root = root;
  }

  public static void setRackCapacity(int rackCapacity) {
    Generator.rackCapacity = rackCapacity;
  }

  @NotNull
  public static List<Candidate> compute(@NotNull LinkedList<Tile> rack, @NotNull BoardSquare[][] board,
                                        @Nullable Comparator<Candidate> ordering) {
    ValidationResult result = validateInput(rack, board);

    if (rack.size() == 0) {
      return Collections.emptyList();
    }

    Set<Candidate> all = new HashSet<>();

    java.util.function.BiConsumer<Integer, Integer> generateAtHook = (x, y) -> {
      for (Direction dir : Direction.primary) {
        generate(x, y, x, y, rack, new LinkedList<>(), all, root, dir, board, result.dimensions);
      }
    };

    if (result.existingTileCount == 0) {
      int midpoint = result.dimensions / 2;
      generateAtHook.accept(midpoint, midpoint);
    } else {
      for (int y = 0; y < result.dimensions; y++) {
        for (int x = 0; x < result.dimensions; x++) {
          if (board[y][x].getTile() == null) {
            for (Direction d : Direction.all) {
              if (d.nextTile(x, y, board) != null) {
                generateAtHook.accept(x, y);
                break;
              }
            }
          }
        }
      }
    }

    List<Candidate> candidates = new ArrayList<>(all);
    if (ordering != null) {
      candidates.sort(ordering);
    }
    return candidates;
  }

  @NotNull
  @Contract(pure = true)
  public static Comparator<Candidate> getDefaultOrdering() {
    return (one, two) -> {
      int scoreDiff = two.getScore() - one.getScore();
      if (scoreDiff != 0) {
        return scoreDiff;
      }
      StringBuilder oneSerialized = new StringBuilder();
      for (TilePlacement placement : one.getPrimary()) {
        oneSerialized.append(placement.getTile().getResolvedLetter());
      }
      StringBuilder twoSerialized = new StringBuilder();
      for (TilePlacement placement : two.getPrimary()) {
        twoSerialized.append(placement.getTile().getResolvedLetter());
      }
      int serializedDiff = oneSerialized.toString().compareTo(twoSerialized.toString());
      if (serializedDiff != 0) {
        return serializedDiff;
      }
      return one.getDirection().name().compareTo(two.getDirection().name());
    };
  }

  @NotNull
  @Contract("_, _ -> new")
  private static ValidationResult validateInput(@NotNull LinkedList<Tile> rack, @NotNull BoardSquare[][] board)
          throws UnsetRootException, UnsetRackCapacityException,
          InvalidBoardStateException, InvalidRackLengthException {
    if (root == null) {
      throw new UnsetRootException();
    }
    if (rackCapacity == null) {
      throw new UnsetRackCapacityException();
    }
    int dimensions = board.length;
    if (dimensions < 3 || dimensions % 2 == 0) {
      throw new InvalidBoardStateException();
    }
    int existingTileCount = 0;
    for (BoardSquare[] minor : board) {
      if (minor.length != dimensions) {
        throw new InvalidBoardStateException();
      }
      for (BoardSquare unit : minor) {
        if (unit.getMultiplier() == null) {
          throw new InvalidBoardStateException();
        }
        if (unit.getTile() != null) {
          existingTileCount++;
        }
      }
    }
    if (rack.size() > rackCapacity) {
      throw new InvalidRackLengthException(rackCapacity, rack.size());
    }
    return new ValidationResult(dimensions, existingTileCount);
  }

  private static void generate(
          int hX, int hY, int x, int y, @NotNull LinkedList<Tile> rack, @NotNull LinkedList<CrossedTilePlacement> placed,
          @NotNull Set<Candidate> all, @NotNull  TrieNode node, @NotNull Direction dir,
          @NotNull BoardSquare[][] board, int dimensions) {
    Tile existingTile = board[y][x].getTile();
    Direction inv = dir.inverse();
    TrieNode childNode;

    java.util.function.Consumer<TrieNode> evaluateAndProceed = child -> {
      if (child.getTerminal() && dir.nextTile(x, y, board) == null) {
        if ((dir.equals(Direction.LEFT) || dir.equals(Direction.UP)) || inv.nextTile(hX, hY, board) == null) {
          all.add(buildCandidate(board, placed, dir));
        }
      }
      Coordinates next;
      TrieNode crossAnchor;
      if ((next = dir.nextCoordinates(x, y, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, all, child, dir, board, dimensions);
      } else if ((crossAnchor = child.getChild(Trie.DELIMITER)) != null && (next = inv.nextCoordinates(hX, hY, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, all, crossAnchor, inv, board, dimensions);
      }
    };

    if (existingTile == null) {
      int currentPlacedCount = placed.size();
      int rackCount = rack.size();

      if (rackCount > 0) {
        Set<Character> visited = new HashSet<>();

        for (int r = 0; r < rackCount; r++) {
          Tile toPlace = rack.removeFirst();

          java.util.function.BiConsumer<Character, Boolean> tryLetterPlacement = (letter, isBlank) -> {
            Tile resolvedTile = isBlank ? new Tile(toPlace.getLetter(), toPlace.getValue(), letter) : toPlace;
            TrieNode child;
            Set<TilePlacement> cross;
            if ((child = node.getChild(letter)) != null && (cross = computeCrossWord(x, y, resolvedTile, dir, board)) != null) {
              TilePlacement root = new TilePlacement(x, y, resolvedTile);
              Set<TilePlacement> resolvedCross = cross.size() > 0 ? cross : null;
              placed.add(new CrossedTilePlacement(root, resolvedCross));
              evaluateAndProceed.accept(child);
              while (placed.size() > currentPlacedCount) {
                placed.removeLast();
              }
            }
          };

          char letter = toPlace.getLetter();
          if (!visited.contains(letter)) {
            visited.add(letter);
            if (letter == Tile.BLANK) {
              for (char l : Alphabet.letters) {
                tryLetterPlacement.accept(l, true);
              }
            } else {
              tryLetterPlacement.accept(letter, false);
            }
          }

          rack.add(toPlace);
        }
      }

      TrieNode crossAnchor;
      Coordinates next;
      if (currentPlacedCount > 0 && (crossAnchor = node.getChild(Trie.DELIMITER)) != null && (next = inv.nextCoordinates(hX, hY, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, all, crossAnchor, inv, board, dimensions);
      }
    } else if ((childNode = node.getChild(existingTile.getResolvedLetter())) != null) {
      placed.add(new CrossedTilePlacement(new TilePlacement(x, y, existingTile, true), null));
      evaluateAndProceed.accept(childNode);
    }
  }

  @Nullable
  private static Set<TilePlacement> computeCrossWord(int sX, int sY, @NotNull Tile toPlace, @NotNull Direction dir,
                                                     @NotNull BoardSquare[][] board) {
    dir = dir.perpendicular();
    if (dir.nextTile(sX, sY, board) == null && dir.inverse().nextTile(sX, sY, board) == null) {
      return Collections.emptySet();
    }
    Set<TilePlacement> placements = new HashSet<>();
    Tile tile = toPlace;
    TrieNode node = root;
    int x = sX;
    int y = sY;
    Direction original = dir;

    while (tile != null) {
      placements.add(new TilePlacement(x, y, tile));
      if ((node = node.getChild(tile.getResolvedLetter())) == null) {
        break;
      }
      TilePlacement next;
      if ((next = dir.nextTile(x, y, board)) != null) {
        x = next.getX();
        y = next.getY();
        tile = next.getTile();
      } else {
        dir = dir.inverse();
        if (dir.equals(original) || (next = dir.nextTile(sX, sY, board)) == null) {
          break;
        }
        x = next.getX();
        y = next.getY();
        tile = next.getTile();
        if (tile != null && (node = node.getChild(Trie.DELIMITER)) == null) {
          break;
        }
      }
    }

    if (node != null && node.getTerminal()) {
      return placements;
    }
    return null;
  }

  private static Candidate buildCandidate(@NotNull BoardSquare[][] board, @NotNull List<CrossedTilePlacement> placements,
                                          @NotNull Direction dir) {
    Set<Set<TilePlacement>> crosses = new HashSet<>();
    Set<TilePlacement> primary = new HashSet<>();

    for (CrossedTilePlacement placement : placements) {
      primary.add(placement.getRoot());
      if (placement.getCross() != null) {
        crosses.add(placement.getCross());
      }
    }

    int score = computeWordScore(board, primary);
    for (Set<TilePlacement> word : crosses) {
      score += computeWordScore(board, word);
    }

    return new Candidate(primary, crosses, dir.normalize(), score);
  }

  private static int computeWordScore(@NotNull BoardSquare[][] board, @NotNull Set<TilePlacement> placements) {
    int wordMultiplier = 1;
    int newTiles = 0, sum = 0;
    for (TilePlacement placement : placements) {
      Tile tile = placement.getTile();
      BoardSquare state = board[placement.getY()][placement.getX()];
      if (state.getTile() == null) {
        newTiles++;
      }
      if (state.getMultiplier() == null || state.getTile() != null) {
        sum += tile.getValue();
      } else {
        int letterValue = state.getMultiplier().getLetterValue();
        int wordValue = state.getMultiplier().getWordValue();
        sum += (letterValue * tile.getValue());
        wordMultiplier *= wordValue;
      }
    }

    int total = sum * wordMultiplier;
    if (newTiles == rackCapacity) {
      total += 50;
    }
    return total;
  }

}
