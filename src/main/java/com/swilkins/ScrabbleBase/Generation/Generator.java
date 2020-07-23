package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.Coordinates;
import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardStateUnit;
import com.swilkins.ScrabbleBase.Board.State.Rack;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.Direction;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidBoardStateException;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidRackLengthException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRackCapacityException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRootException;
import com.swilkins.ScrabbleBase.Generation.Objects.EnrichedTilePlacement;
import com.swilkins.ScrabbleBase.Generation.Objects.ScoredCandidate;
import com.swilkins.ScrabbleBase.Vocabulary.Alphabet;
import com.swilkins.ScrabbleBase.Vocabulary.Trie;
import com.swilkins.ScrabbleBase.Vocabulary.TrieNode;

import java.util.*;

/**
 * Contains logic for exhaustive move generation
 * given game context.
 */
public class Generator {

  private static TrieNode root = null;
  private static Integer rackCapacity = null;

  public static void setRoot(TrieNode root) {
    Generator.root = root;
  }

  public static void setRackCapacity(int rackCapacity) {
    Generator.rackCapacity = rackCapacity;
  }

  public static List<ScoredCandidate> computeAllCandidates(Rack rack, BoardStateUnit[][] board) {
    ValidationResult result = validateInput(rack, board);

    if (rack.size() == 0) {
      return Collections.emptyList();
    }

    Set<ScoredCandidate> all = new HashSet<>();

    java.util.function.BiConsumer<Integer, Integer> generateAtHook = (x, y) -> {
      for (Direction d : Direction.primary) {
        generate(x, y, x, y, rack, new LinkedList<>(), 0, all, root, d, board, result.dimensions);
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

    List<ScoredCandidate> collected = new ArrayList<>(all);
    collected.sort(getDefaultOrdering());
    return collected;
  }

  public static Comparator<ScoredCandidate> getDefaultOrdering() {
    return (one, two) -> {
      int scoreDiff = two.getScore() - one.getScore();
      if (scoreDiff != 0) {
        return scoreDiff;
      }
      StringBuilder oneSerialized = new StringBuilder();
      for (TilePlacement placement : one.getPlacements()) {
        oneSerialized.append(placement.getTile().getResolvedLetter());
      }
      StringBuilder twoSerialized = new StringBuilder();
      for (TilePlacement placement : two.getPlacements()) {
        twoSerialized.append(placement.getTile().getResolvedLetter());
      }
      int serializedDiff = oneSerialized.toString().compareTo(twoSerialized.toString());
      if (serializedDiff != 0) {
        return serializedDiff;
      }
      return one.getDirection().name().compareTo(two.getDirection().name());
    };
  }

  private static ValidationResult validateInput(Rack rack, BoardStateUnit[][] board)
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
    for (BoardStateUnit[] minor : board) {
      if (minor.length != dimensions) {
        throw new InvalidBoardStateException();
      }
      for (BoardStateUnit unit : minor) {
        if (unit == null || unit.getMultiplier() == null) {
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

  private static class ValidationResult {

    private final int dimensions;
    private final int existingTileCount;

    public ValidationResult(int dimensions, int existingTileCount) {
      this.dimensions = dimensions;
      this.existingTileCount = existingTileCount;
    }

  }

  private static void generate(
          int hX, int hY, int x, int y, Rack rack, LinkedList<EnrichedTilePlacement> placed,
          final int accumulated, Set<ScoredCandidate> all, TrieNode node,
          Direction d, BoardStateUnit[][] board, int dimensions) {
    Tile existingTile = board[y][x].getTile();
    Direction i = d.inverse();
    TrieNode childNode;

    java.util.function.BiConsumer<TrieNode, Integer> evaluateAndProceed = (child, score) -> {
      int totalScore;
      if (child.getTerminal() && d.nextTile(x, y, board) == null) {
        if ((d.equals(Direction.LEFT) || d.equals(Direction.UP)) || i.nextTile(hX, hY, board) == null) {
          if ((totalScore = applyScorer(board, placed, score)) > 0) {
            List<TilePlacement> placements = new ArrayList<>();
            for (EnrichedTilePlacement placement : placed) {
              placements.add(placement.getRoot());
            }
            Direction normalized = d.normalize();
            placements.sort(Direction.along(normalized));
            all.add(new ScoredCandidate(placements, normalized.name(), totalScore));
          }
        }
      }
      Coordinates next;
      TrieNode crossAnchor;
      if ((next = d.nextCoordinates(x, y, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, score, all, child, d, board, dimensions);
      } else if ((crossAnchor = child.getChild(Trie.DELIMITER)) != null && (next = i.nextCoordinates(hX, hY, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, score, all, crossAnchor, i, board, dimensions);
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
            List<TilePlacement> cross;
            if ((child = node.getChild(letter)) != null && (cross = computeCrossWord(x, y, resolvedTile, d, board)) != null) {
              TilePlacement root = new TilePlacement(x, y, resolvedTile);
              List<TilePlacement> resolvedCross = cross.size() > 0 ? cross : null;
              placed.add(new EnrichedTilePlacement(root, resolvedCross));
              evaluateAndProceed.accept(child, accumulated);
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
      if (currentPlacedCount > 0 && (crossAnchor = node.getChild(Trie.DELIMITER)) != null && (next = i.nextCoordinates(hX, hY, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, accumulated, all, crossAnchor, i, board, dimensions);
      }
    } else if (node != null && (childNode = node.getChild(existingTile.getResolvedLetter())) != null) {
      evaluateAndProceed.accept(childNode, accumulated + existingTile.getValue());
    }
  }

  private static List<TilePlacement> computeCrossWord(int sX, int sY, Tile toPlace, Direction d, BoardStateUnit[][] board) {
    d = d.perpendicular();
    if (d.nextTile(sX, sY, board) == null && d.inverse().nextTile(sX, sY, board) == null) {
      return Collections.emptyList();
    }
    List<TilePlacement> placements = new ArrayList<>();
    Tile tile = toPlace;
    TrieNode node = root;
    int x = sX;
    int y = sY;
    Direction original = d;

    while (tile != null) {
      placements.add(new TilePlacement(x, y, tile));
      if ((node = node.getChild(tile.getResolvedLetter())) == null) {
        break;
      }
      TilePlacement next;
      if ((next = d.nextTile(x, y, board)) != null) {
        x = next.getX();
        y = next.getY();
        tile = next.getTile();
      } else {
        d = d.inverse();
        if (d.equals(original) || (next = d.nextTile(sX, sY, board)) == null) {
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
      placements.sort(Direction.along(d));
      return placements;
    }
    return null;
  }

  private static int applyScorer(BoardStateUnit[][] board, List<EnrichedTilePlacement> placements, int accumulated) {
    List<List<TilePlacement>> crosses = new ArrayList<>(placements.size());
    List<TilePlacement> primary = new ArrayList<>(placements.size());

    for (EnrichedTilePlacement placement : placements) {
      primary.add(placement.getRoot());
      if (placement.getCross() != null) {
        crosses.add(placement.getCross());
      }
    }

    int sum = computeScoreOf(board, primary, accumulated);
    for (List<TilePlacement> word : crosses) {
      sum += computeScoreOf(board, word, 0);
    }

    return sum;
  }

  private static int computeScoreOf(BoardStateUnit[][] board, List<TilePlacement> placements, int accumulated) {
    int wordMultiplier = 1;
    int newTiles = 0;
    int sum = accumulated;

    for (TilePlacement placement : placements) {
      Tile tile = placement.getTile();
      BoardStateUnit state = board[placement.getY()][placement.getX()];
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
