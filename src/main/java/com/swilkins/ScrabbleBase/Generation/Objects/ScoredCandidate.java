package com.swilkins.ScrabbleBase.Generation.Objects;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.DirectionName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoredCandidate {

  private final List<TilePlacement> primary;
  private List<List<TilePlacement>> crosses = null;
  private final DirectionName direction;
  private final int score;
  private List<String> serialized = null;

  public ScoredCandidate(List<TilePlacement> primary, List<List<TilePlacement>> crosses, DirectionName direction, int score) {
    this.primary = primary;
    if (crosses != null && !crosses.isEmpty()) {
      this.crosses = crosses;
    }
    this.direction = direction;
    this.score = score;
  }

  public List<TilePlacement> getPrimary() {
    return primary;
  }

  public DirectionName getDirection() {
    return direction;
  }

  public List<List<TilePlacement>> getCrosses() {
    return crosses;
  }

  public List<String> getSerialized() {
    if (serialized == null) {
      serialized = new ArrayList<>();
      StringBuilder builder = new StringBuilder();
      for (TilePlacement placement : primary) {
        builder.append(placement.getTile().getResolvedLetter());
      }
      serialized.add(builder.toString());
      if (crosses != null) {
        for (List<TilePlacement> cross : crosses) {
          builder = new StringBuilder();
          for (TilePlacement placement : cross) {
            builder.append(placement.getTile().getResolvedLetter());
          }
          serialized.add(builder.toString());
        }
      }
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
            primary.equals(that.primary) &&
            direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(primary, direction.name(), score);
  }

  @Override
  public String toString() {
    StringBuilder word = new StringBuilder();
    List<String> locations = new ArrayList<>();
    for (TilePlacement p : this.primary) {
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
