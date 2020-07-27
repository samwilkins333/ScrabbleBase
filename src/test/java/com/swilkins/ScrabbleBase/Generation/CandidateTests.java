package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.swilkins.ScrabbleBase.Board.Configuration.getStandardTile;
import static org.junit.Assert.assertEquals;

public class CandidateTests {

  @Test
  public void scoredCandidateHashingAndEquality() {
    Candidate first, second;
    Set<Candidate> collector;

    Set<TilePlacement> firstPlacements = new HashSet<>();
    Set<TilePlacement> secondPlacements = new HashSet<>();
    char[] letters = {'a', 'u', Tile.BLANK, 's', 'e', 'r', 'o'};
    for (int i = 0; i < letters.length; i++) {
      firstPlacements.add(new TilePlacement(i, 7, getStandardTile(letters[i])));
      secondPlacements.add(new TilePlacement(i, 7, getStandardTile(letters[i])));
    }
    first = new Candidate(firstPlacements, null, Direction.RIGHT, 0);
    second = new Candidate(secondPlacements, null, Direction.RIGHT, 0);
    collector = new HashSet<>();
    collector.add(first);
    collector.add(second);
    assertEquals(first, second);
    assertEquals(1, collector.size());

    Set<Set<TilePlacement>> firstCrosses = new HashSet<>();
    Set<TilePlacement> firstCross = new HashSet<>();
    firstCross.add(new TilePlacement(7, 7, getStandardTile('o')));
    firstCross.add(new TilePlacement(7, 6, getStandardTile('t')));
    firstCrosses.add(firstCross);

    Set<Set<TilePlacement>> secondCrosses = new HashSet<>();
    Set<TilePlacement> secondCross = new HashSet<>();
    secondCross.add(new TilePlacement(7, 6, getStandardTile('t')));
    secondCross.add(new TilePlacement(7, 7, getStandardTile('o')));
    secondCrosses.add(secondCross);

    assertEquals(firstCross, secondCross);

    first = new Candidate(firstPlacements, firstCrosses, Direction.RIGHT, 0);
    second = new Candidate(secondPlacements, secondCrosses, Direction.RIGHT, 0);
    collector = new HashSet<>();
    collector.add(first);
    collector.add(second);
    assertEquals(first, second);
    assertEquals(1, collector.size());

    first = new Candidate(firstPlacements, new HashSet<>(), Direction.RIGHT, 0);
    second = new Candidate(secondPlacements, new HashSet<>(), Direction.RIGHT, 0);
    collector = new HashSet<>();
    collector.add(first);
    collector.add(second);
    assertEquals(first, second);
    assertEquals(1, collector.size());
  }

}
