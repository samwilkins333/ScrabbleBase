package ScrabbleBase;

import java.util.List;
import java.util.stream.Collectors;

public class ScoredCandidate {

  private final List<TilePlacement> placements;
  private final DirectionName direction;
  private final int score;

  public ScoredCandidate(List<TilePlacement> placements, DirectionName direction, int score) {
    this.placements = placements;
    this.direction = direction;
    this.score = score;
  }

  public List<TilePlacement> getPlacements() {
    return placements;
  }

  public DirectionName getDirection() {
    return direction;
  }

  public int getScore() {
    return score;
  }

  @Override
  public String toString() {
    String word = this.placements.stream().map(p -> {
      Tile tile = p.getTile();
      String resolved = tile.getLetterProxy() != null ? String.valueOf(tile.getLetterProxy()) : "";
      return String.format("%s:%c(%d, %d)", resolved, tile.getLetter(), p.getX(), p.getY());
    }).collect(Collectors.joining(" "));
    return word + " [" + this.score + "] " + this.direction.name();
  }

}
