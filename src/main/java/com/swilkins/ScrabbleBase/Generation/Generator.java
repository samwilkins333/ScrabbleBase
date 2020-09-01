package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.Coordinates;
import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardSquare;
import com.swilkins.ScrabbleBase.Board.State.Multiplier;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidBoardStateException;
import com.swilkins.ScrabbleBase.Generation.Exception.InvalidRackLengthException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRackCapacityException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetTrieException;
import com.swilkins.ScrabbleBase.Vocabulary.PermutationTrie;
import com.swilkins.ScrabbleBase.Vocabulary.TrieNode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.swilkins.ScrabbleBase.Board.Configuration.STANDARD_BINGO;

/**
 * Represents an entity capable of generating an exhaustive list of
 * candidate plays (represented for utility by a <code>GeneratorResult</code>
 * instance) given rack state, board state and vocabulary (trie) state.
 * It supports the use of blank tiles.
 */
public class Generator {

  // The specialized data structure used to store the vocabulary
  private PermutationTrie trie;
  // The root of the trie representing the vocabulary. All word traversals start here.
  private TrieNode root;
  // The delimiter used to denote a direction inversion in the PermutationTrie
  private char delimiter;
  // A direct result of the trie's contents, not necessarily just [a-z]
  private Set<Character> alphabet;
  // Value used to determine when a candidate exhausts the rack and thus invokes the bonus score, or 'bingo'
  private Integer rackCapacity;

  /**
   * If the caller has references to both the <code>PermutationTrie</code> representing
   * the vocabulary and the magnitude of the rack capacity, this constructor should be used.
   *
   * @param trie         the permutation trie instance to be used in candidate generation
   * @param rackCapacity the number of tiles in a full rack
   * @throws IllegalArgumentException if the trie reference is {@code null}, or the rack capacity is not
   *                                  a positive number
   */
  public Generator(PermutationTrie trie, int rackCapacity) throws IllegalArgumentException {
    this.setTrie(trie);
    this.setRackCapacity(rackCapacity);
  }

  /**
   * If the caller does not have references to both the <code>PermutationTrie</code> representing
   * the vocabulary and the magnitude of the rack capacity, this constructor should be used.
   * Then, these two properties can be later set by invoking the appropriate setter.
   */
  public Generator() {
    this.trie = null;
    this.rackCapacity = null;
  }

  /**
   * @return the permutation trie instance to be used in candidate generation
   */
  public PermutationTrie getPermutationTrie() {
    return this.trie;
  }

  /**
   * Directs this <code>Generator</code> to use the given instance of
   * <code>PermutationTrie</code> in candidate generation.
   *
   * @param trie the permutation trie instance to be used in candidate generation
   * @throws IllegalArgumentException if the trie reference is {@code null}
   */
  public void setTrie(PermutationTrie trie) throws IllegalArgumentException {
    if (trie == null) {
      throw new IllegalArgumentException();
    }
    this.delimiter = (this.trie = trie).getDelimiter();
  }

  public Integer getRackCapacity() {
    return rackCapacity;
  }

  public void setRackCapacity(int rackCapacity) throws IllegalArgumentException {
    if (rackCapacity <= 0) {
      throw new IllegalArgumentException();
    }
    this.rackCapacity = rackCapacity;
  }

  public static Comparator<Candidate> getDefaultOrdering() {
    return (one, two) -> {
      // First, order by score
      int scoreDiff = two.getScore() - one.getScore();
      if (scoreDiff != 0) {
        return scoreDiff;
      }
      // Break ties by ordering lexicographically
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
      // If the same word appears in multiple places, divide into clusters of the same orientation
      return one.getDirection().name().compareTo(two.getDirection().name());
    };
  }

  public GeneratorResult compute(LinkedList<Tile> rack, BoardSquare[][] board)
          throws IllegalArgumentException, UnsetTrieException, UnsetRackCapacityException {
    Set<Coordinates> validHooks = validateInput(rack, board);
    int dimensions = board.length;

    this.alphabet = this.trie.getAlphabet();
    this.root = this.trie.getRoot();

    Set<Candidate> candidates = new HashSet<>();

    if (!rack.isEmpty() && !this.trie.isEmpty()) {

      Consumer<Coordinates> generateAtHook = coordinates -> {
        int x = coordinates.getX();
        int y = coordinates.getY();
        for (Direction dir : Direction.primary) {
          generate(x, y, x, y, rack, new LinkedList<>(), candidates, this.root, dir, board, dimensions);
        }
      };

      if (validHooks.isEmpty()) {
        int midpoint = dimensions / 2;
        generateAtHook.accept(new Coordinates(midpoint, midpoint));
      } else {
        validHooks.forEach(generateAtHook);
      }
    }

    return new GeneratorResult(candidates);
  }

  private Set<Coordinates> validateInput(LinkedList<Tile> rack, BoardSquare[][] board)
          throws UnsetTrieException, UnsetRackCapacityException,
          InvalidBoardStateException, InvalidRackLengthException {
    if (rack == null || board == null) {
      throw new IllegalArgumentException();
    }
    if (this.trie == null) {
      throw new UnsetTrieException();
    }
    if (rackCapacity == null) {
      throw new UnsetRackCapacityException();
    }
    if (rack.size() > rackCapacity) {
      throw new InvalidRackLengthException(rackCapacity, rack.size());
    }
    int dimensions = board.length;
    if (dimensions < 3 || dimensions % 2 == 0) {
      throw new InvalidBoardStateException();
    }
    Set<Coordinates> validHooks = new HashSet<>();
    for (int y = 0; y < dimensions; y++) {
      BoardSquare[] minor = board[y];
      if (minor.length != dimensions) {
        throw new InvalidBoardStateException();
      }
      for (int x = 0; x < dimensions; x++) {
        BoardSquare square = minor[x];
        if (square.getMultiplier() == null) {
          throw new InvalidBoardStateException();
        }
        if (square.getTile() == null) {
          for (Direction dir : Direction.all) {
            if (dir.nextTile(x, y, board) != null) {
              validHooks.add(new Coordinates(x, y));
              break;
            }
          }
        }
      }
    }
    return validHooks;
  }

  private void generate(
          int hX, int hY, int x, int y, LinkedList<Tile> rack, LinkedList<CrossedTilePlacement> placed,
          Set<Candidate> all, TrieNode node, Direction dir,
          BoardSquare[][] board, int dimensions) {
    Tile existingTile = board[y][x].getTile();
    Direction inv = dir.inverse();
    TrieNode childNode;

    Consumer<TrieNode> evaluateAndProceed = child -> {
      if (child.getTerminal() && dir.nextTile(x, y, board) == null &&
              (dir.equals(Direction.LEFT) || dir.equals(Direction.UP) || inv.nextTile(hX, hY, board) == null)) {
        all.add(buildCandidate(board, placed, dir));
      }
      Coordinates next;
      TrieNode crossAnchor;
      if ((next = dir.nextCoordinates(x, y, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, all, child, dir, board, dimensions);
      } else if ((crossAnchor = child.getChild(this.delimiter)) != null && (next = inv.nextCoordinates(hX, hY, dimensions)) != null) {
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

          BiConsumer<Character, Boolean> tryLetterPlacement = (letter, isBlank) -> {
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
              for (char l : this.alphabet) {
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
      if (currentPlacedCount > 0 && (crossAnchor = node.getChild(this.delimiter)) != null && (next = inv.nextCoordinates(hX, hY, dimensions)) != null) {
        generate(hX, hY, next.getX(), next.getY(), rack, placed, all, crossAnchor, inv, board, dimensions);
      }
    } else if ((childNode = node.getChild(existingTile.getResolvedLetter())) != null) {
      placed.add(new CrossedTilePlacement(new TilePlacement(x, y, existingTile, true), null));
      evaluateAndProceed.accept(childNode);
    }
  }

  private Set<TilePlacement> computeCrossWord(int sX, int sY, Tile toPlace, Direction dir,
                                              BoardSquare[][] board) {
    dir = dir.perpendicular();
    if (dir.nextTile(sX, sY, board) == null && dir.inverse().nextTile(sX, sY, board) == null) {
      return Collections.emptySet();
    }
    Set<TilePlacement> placements = new HashSet<>();
    Tile tile = toPlace;
    TrieNode node = this.root;
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
        if (tile != null && (node = node.getChild(this.delimiter)) == null) {
          break;
        }
      }
    }

    if (node != null && node.getTerminal()) {
      return placements;
    }
    return null;
  }

  private Candidate buildCandidate(BoardSquare[][] board, List<CrossedTilePlacement> placements,
                                   Direction dir) {
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

  private int computeWordScore(BoardSquare[][] board, Set<TilePlacement> placements) {
    int wordMultiplier = 1;
    int newTiles = 0, sum = 0;
    for (TilePlacement placement : placements) {
      Tile tile = placement.getTile();
      BoardSquare state = board[placement.getY()][placement.getX()];
      if (state.getTile() == null) {
        newTiles++;
      }
      Multiplier multiplier = state.getMultiplier();
      if (multiplier == null || state.getTile() != null) {
        sum += tile.getValue();
      } else {
        sum += (multiplier.getLetterValue() * tile.getValue());
        wordMultiplier *= multiplier.getWordValue();
      }
    }

    int total = sum * wordMultiplier;
    if (newTiles == rackCapacity) {
      total += STANDARD_BINGO;
    }
    return total;
  }

}