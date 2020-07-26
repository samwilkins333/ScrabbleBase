package com.swilkins.ScrabbleBase.Generation.Objects;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.DirectionName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoredCandidate {

  private final List<TilePlacement> placements;
  private final DirectionName direction;
  private final int score;
  private String serialized = null;

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

  public String getSerialized() {
    if (serialized == null) {
      StringBuilder builder = new StringBuilder();
      for (TilePlacement placement : placements) {
        builder.append(placement.getTile().getResolvedLetter());
      }
      serialized = builder.toString();
    }
    return serialized;
  }

  public int getScore() {
    return score;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScoredCandidate that = (ScoredCandidate) o;
    return score == that.score &&
            placements.equals(that.placements) &&
            direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(placements, direction.name(), score);
  }

  @Override
  public String toString() {
    StringBuilder word = new StringBuilder();
    List<String> locations = new ArrayList<>();
    for (TilePlacement p : this.placements) {
      Tile tile = p.getTile();
      String resolved;
      if (tile.getLetterProxy() != null) {
        resolved = String.format("{%s*}", tile.getLetterProxy());
      } else {
        resolved = String.valueOf(tile.getLetter());
      }
      word.append(resolved);
      String placementString = String.format("%s", resolved);
      if (!p.isExisting()) {
        placementString = String.format("%s(%s, %s)", placementString, p.getX(), p.getY());
      }
      locations.add(placementString);
    }
    return word + " (" + this.score + ") @ [" + String.join(", ", locations) + "] " + this.direction.name();
  }

}
