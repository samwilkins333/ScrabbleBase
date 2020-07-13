package ScrabbleBase.Generation;

import ScrabbleBase.Board.State.BoardStateUnit;
import ScrabbleBase.Board.Location.Coordinates;
import ScrabbleBase.Board.State.Tile;
import ScrabbleBase.Board.Location.TilePlacement;
import ScrabbleBase.Generation.Direction.Direction;
import ScrabbleBase.Generation.Exception.*;
import ScrabbleBase.Generation.Objects.EnrichedTilePlacement;
import ScrabbleBase.Generation.Objects.ScoredCandidate;
import ScrabbleBase.Generation.Objects.SerializationResult;
import ScrabbleBase.Vocabulary.Alphabet;
import ScrabbleBase.Vocabulary.Trie;
import ScrabbleBase.Vocabulary.TrieNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains logic for exhaustive move generation
 * given game context.
 */
public class Generator {

  public static final Generator Instance = new Generator();
  private TrieNode root = null;
  private Integer rackCapacity = null;

  public void setRoot(TrieNode root) {
    this.root = root;
  }

  public void setRackCapacity(int rackCapacity) {
    this.rackCapacity = rackCapacity;
  }

  public List<ScoredCandidate> computeAllCandidates(LinkedList<Tile> rack, BoardStateUnit[][] played, int movesMade)
  {
    int dimensions = this.validateInput(rack, played, movesMade);

    List<ScoredCandidate> all = new ArrayList<>();
    if (rack.size() > 0) {
      Set<String> unique = new HashSet<>();

      java.util.function.BiConsumer<Integer, Integer> generateAtHook = (x, y) -> {
        for (Direction d : Direction.primary) {
          this.generate(x, y, x, y, rack, new LinkedList<>(), 0, all, unique, this.root, d, played);
        }
      };

      if (movesMade == 0) {
        int midpoint = dimensions / 2;
        generateAtHook.accept(midpoint, midpoint);
      } else {
        for (int y = 0; y < dimensions; y++) {
          for (int x = 0; x < dimensions; x++) {
            if (played[y][x].getTile() == null) {
              for (Direction d : Direction.all) {
                if (d.nextTile(x, y, played) != null) {
                  generateAtHook.accept(x, y);
                  break;
                }
              }
            }
          }
        }
      }

      all.sort((one, two) -> {
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
      });
    }

    return all;
  }

  private int validateInput(LinkedList<Tile> rack, BoardStateUnit[][] played, int movesMade)
          throws UnsetRootException, UnsetRackCapacityException, InvalidMovesMadeException,
          InvalidBoardStateException, InvalidRackLengthException
  {
    if (this.root == null) {
      throw new UnsetRootException();
    }
    if (this.rackCapacity == null) {
      throw new UnsetRackCapacityException();
    }
    if (movesMade < 0) {
      throw new InvalidMovesMadeException();
    }
    int dimensions = played.length;
    if (dimensions < 3 || dimensions % 2 == 0) {
      throw new InvalidBoardStateException();
    }
    for (BoardStateUnit[] minor : played) {
      if (minor.length != dimensions) {
        throw new InvalidBoardStateException();
      }
      for (BoardStateUnit unit : minor) {
        if (unit == null) {
          throw new InvalidBoardStateException();
        }
      }
    }
    if (rack.size() > this.rackCapacity) {
      throw new InvalidRackLengthException(this.rackCapacity, rack.size());
    }
    return dimensions;
  }

  private void generate(
          int hX, int hY, int x, int y, LinkedList<Tile> rack, LinkedList<EnrichedTilePlacement> placed,
          final int accumulated, List<ScoredCandidate> all, Set<String> unique, TrieNode node,
          Direction d, BoardStateUnit[][] played)
  {
    Tile tile = played[y][x].getTile();
    Direction i = d.inverse();
    TrieNode childNode;

    java.util.function.BiConsumer<TrieNode, Integer> evaluateAndProceed = (child, accumulatedScore) -> {
      if (child.getTerminal() && d.nextTile(x, y, played) == null) {
        int scored;
        if ((scored = this.applyScorer(played, placed, accumulatedScore)) > 0) {
          List<TilePlacement> placements = placed.stream().map(EnrichedTilePlacement::getRoot).collect(Collectors.toList());
          SerializationResult result = this.contextSerialize(placements, d);
          if (!unique.contains(result.getSerialized())) {
            unique.add(result.getSerialized());
            all.add(new ScoredCandidate(placements, result.getNormalized(), scored));
          }
        }
      }
      Coordinates next;
      TrieNode crossAnchor;
      if ((next = d.nextCoordinates(x, y)) != null) {
        this.generate(hX, hY, next.getX(), next.getY(), rack, placed, accumulatedScore, all, unique, child, d, played);
      } else if ((crossAnchor = child.getChild(Trie.DELIMITER)) != null && (next = i.nextCoordinates(hX, hY)) != null) {
        this.generate(hX, hY, next.getX(), next.getY(), rack, placed, accumulatedScore, all, unique, crossAnchor, i, played);
      }
    };

    if (tile == null) {
      int currentPlacedCount = placed.size();
      int rackCount = rack.size();

      if (rackCount > 0) {
        Set<Character> visited = new HashSet<>();

        for (int r = 0; r < rackCount; r++) {
          Tile toPlace = rack.removeFirst();

          java.util.function.BiConsumer<Character, Boolean> tryLetterPlacement = (letter, isBlank) -> {
            Tile resolvedTile = isBlank ? new Tile(toPlace.getLetter(), toPlace.getScore(), letter) : toPlace;
            TrieNode child;
            List<TilePlacement> cross;
            if ((child = node.getChild(letter)) != null && (cross = this.computeCrossWord(x, y, resolvedTile, d, played)) != null) {
              TilePlacement placement = new TilePlacement(x, y, resolvedTile);
              List<TilePlacement> resolvedCross = cross.size() > 0 ? cross : null;
              placed.add(new EnrichedTilePlacement(placement, resolvedCross));
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
              char[] alphabet = Alphabet.letters;
              for (int l = 1; l <= 26; l++) {
                tryLetterPlacement.accept(alphabet[l], true);
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
      if (currentPlacedCount > 0 && (crossAnchor = node.getChild(Trie.DELIMITER)) != null && (next = i.nextCoordinates(hX, hY)) != null) {
        this.generate(hX, hY, next.getX(), next.getY(), rack, placed, accumulated, all, unique, crossAnchor, i, played);
      }
    } else if (node != null && (childNode = node.getChild(tile.getResolvedLetter())) != null) {
      evaluateAndProceed.accept(childNode, accumulated + tile.getScore());
    }
  }

  private List<TilePlacement> computeCrossWord(int sX, int sY, Tile toPlace, Direction d, BoardStateUnit[][] played)
  {
    d = d.perpendicular();
    if (d.nextTile(sX, sY, played) == null && d.inverse().nextTile(sX, sY, played) == null) {
      return Collections.emptyList();
    }
    List<TilePlacement> placements = new ArrayList<>();
    Tile tile = toPlace;
    TrieNode node = this.root;
    int x = sX;
    int y = sY;
    Direction original = d;

    while (tile != null) {
      placements.add(new TilePlacement(x, y, tile));
      if ((node = node.getChild(tile.getResolvedLetter())) == null) {
        break;
      }
      TilePlacement next;
      if ((next = d.nextTile(x, y, played)) != null) {
        x = next.getX();
        y = next.getY();
        tile = next.getTile();
      } else {
        d = d.inverse();
        if (d.equals(original) || (next = d.nextTile(sX, sY, played)) == null) {
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

  private int applyScorer(BoardStateUnit[][] played, List<EnrichedTilePlacement> placements, int accumulated)
  {
    List<List<TilePlacement>> flattened = new ArrayList<>();
    flattened.add(new ArrayList<>(placements.size()));
    for (EnrichedTilePlacement placement : placements) {
      flattened.get(0).add(placement.getRoot());
      if (placement.getCross() != null) {
        flattened.add(placement.getCross());
      }
    }

    int sum = 0;
    for (List<TilePlacement> word : flattened) {
      sum += this.computeScoreOf(played, word, accumulated);
      accumulated = 0;
    }

    return sum;
  }

  private SerializationResult contextSerialize(List<TilePlacement> placements, Direction direction)
  {
    Direction normalized = direction.normalize();
    placements.sort(Direction.along(normalized));
    String serialized = placements.stream().map(p -> {
      Tile tile = p.getTile();
      String resolved =  tile.getLetterProxy() != null ? String.valueOf(tile.getLetterProxy()) : "";
      return String.format("%s:%c%d,%d", resolved, tile.getLetter(), p.getX(), p.getY());
    }).collect(Collectors.joining(","));
    return new SerializationResult(serialized, normalized.name());
  }

  private int computeScoreOf(BoardStateUnit[][] played, List<TilePlacement> placements, int sum)
  {
    int wordMultiplier = 1;
    int newTiles = 0;

    for (TilePlacement placement : placements) {
      Tile tile = placement.getTile();
      BoardStateUnit state = played[placement.getY()][placement.getX()];
      if (state.getTile() == null) {
        newTiles++;
      }
      if (state.getMultiplier() == null || state.getTile() != null) {
        sum += tile.getScore();
      } else {
        int letterValue = state.getMultiplier().getLetterValue();
        int wordValue = state.getMultiplier().getWordValue();
        sum += (letterValue * tile.getScore());
        wordMultiplier *= wordValue;
      }
    }

    int total = wordMultiplier * sum;
    if (newTiles == this.rackCapacity) {
      total += 50;
    }
    return total;
  }

}
