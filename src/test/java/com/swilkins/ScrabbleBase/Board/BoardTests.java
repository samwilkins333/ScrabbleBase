package com.swilkins.ScrabbleBase.Board;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.DirectionName;
import com.swilkins.ScrabbleBase.Generation.Objects.ScoredCandidate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BoardTests {

  @Test
  public void scoredCandidateHashingAndEquality() {
    ScoredCandidate first, second;
    List<TilePlacement> firstPlacements = new ArrayList<>();
    List<TilePlacement> secondPlacements = new ArrayList<>();
    char[] letters = {'a', 'u', Tile.BLANK, 's', 'e', 'r', 'o'};
    int[] values = {1, 1, 0, 1, 1, 1, 1};
    int score = 0;
    for (int i = 0; i < letters.length; i++) {
      char letter = letters[i];
      int value = values[i];
      score += value;
      firstPlacements.add(new TilePlacement(i, 7, new Tile(letter, value, null)));
      secondPlacements.add(new TilePlacement(i, 7, new Tile(letter, value, null)));
    }
    first = new ScoredCandidate(firstPlacements, null, DirectionName.RIGHT, score);
    second = new ScoredCandidate(secondPlacements, null, DirectionName.RIGHT, score);
    Set<ScoredCandidate> collector = new HashSet<>();
    collector.add(first);
    collector.add(second);
    assertEquals(first, second);
    assertEquals(collector.size(), 1);
  }

  @Test
  public void tileHashingAndEquality() {
    Tile first = new Tile('a', 1, null);
    Tile second = new Tile(Tile.BLANK, 0, null);

    assertNotEquals(first, second);

    Tile firstDuplicate = new Tile('a', 1, null);
    Tile secondDuplicate = new Tile(Tile.BLANK, 0, null);

    assertEquals(firstDuplicate, first);
    assertEquals(secondDuplicate, second);
    assertNotEquals(new Tile(Tile.BLANK, 0, 'b'), second);

    Set<Tile> tiles = new HashSet<>();
    tiles.add(first);
    tiles.add(firstDuplicate);
    tiles.add(second);
    tiles.add(secondDuplicate);

    assertEquals(2, tiles.size());
  }

}
