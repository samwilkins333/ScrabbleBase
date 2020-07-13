package ScrabbleBase.Generation.Objects;

import ScrabbleBase.Board.State.Tile;
import ScrabbleBase.Board.Location.TilePlacement;
import ScrabbleBase.Generation.Direction.DirectionName;

import java.util.ArrayList;
import java.util.List;

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
    StringBuilder word = new StringBuilder();
    List<String> locations = new ArrayList<>();
    for (TilePlacement p : this.placements) {
      Tile tile = p.getTile();
      String resolved;
      if (tile.getLetterProxy() != null) {
        resolved = String.format("(%s*)", tile.getLetterProxy());
      } else {
        resolved = String.valueOf(tile.getLetter());
      }
      word.append(resolved);
      locations.add(String.format("%s(%d, %d)", resolved, p.getX(), p.getY()));
    }
    return word + " (" + this.score + ") @ [" + String.join(", ", locations) + "] " + this.direction.name();
  }

}
