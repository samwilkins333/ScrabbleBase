package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.Tile;

import java.util.*;

import static com.swilkins.ScrabbleBase.Generation.Direction.along;
import static com.swilkins.ScrabbleBase.Generation.Direction.crossesAlong;

public class Candidate {

  private final Set<TilePlacement> primarySource;
  private List<TilePlacement> primary = null;
  private Set<Set<TilePlacement>> crossesSource = null;
  private List<List<TilePlacement>> crosses = null;
  private final Direction direction;
  private final int score;
  private List<String> serialized = null;

  public Candidate(Set<TilePlacement> primary, Set<Set<TilePlacement>> crosses, Direction direction, int score) {
    this.primarySource = primary;
    if (crosses != null && !crosses.isEmpty()) {
      this.crossesSource = crosses;
    }
    this.direction = direction;
    this.score = score;
  }

  public List<TilePlacement> getPrimary() {
    if (primary == null) {
      primary = new ArrayList<>(primarySource);
      primary.sort(along(direction));
    }
    return primary;
  }

  public DirectionName getDirection() {
    return direction.name();
  }

  public List<List<TilePlacement>> getCrosses() {
    if (crosses == null && crossesSource != null) {
      crosses = new ArrayList<>(crossesSource.size());
      Comparator<TilePlacement> crossSorter = along(direction.perpendicular());
      for (Set<TilePlacement> crossSource : crossesSource) {
        List<TilePlacement> cross = new ArrayList<>(crossSource);
        cross.sort(crossSorter);
        crosses.add(cross);
      }
      crosses.sort(crossesAlong(direction));
    }
    return crosses;
  }

  public List<String> getSerialized() {
    if (serialized == null) {
      serialized = new ArrayList<>();
      StringBuilder builder = new StringBuilder();
      for (TilePlacement placement : getPrimary()) {
        builder.append(placement.getTile().getResolvedLetter());
      }
      serialized.add(builder.toString());
      if (crosses != null) {
        for (List<TilePlacement> cross : getCrosses()) {
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
    Candidate that = (Candidate) o;
    boolean equals = score == that.score &&
            primarySource.equals(that.primarySource) &&
            direction == that.direction;
    if (crossesSource == null && that.crossesSource == null) {
      return equals;
    }
    if (crossesSource == null || that.crossesSource == null) {
      return false;
    }
    return equals && crossesSource.equals(that.crossesSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primarySource, direction, score, crossesSource);
  }

  @Override
  public String toString() {
    StringBuilder word = new StringBuilder();
    List<String> locations = new ArrayList<>();
    for (TilePlacement p : this.getPrimary()) {
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
    return word + " (" + this.score + ") @ [" + String.join(", ", locations) + "] " + this.direction.name() + " " + getCrosses();
  }

}
