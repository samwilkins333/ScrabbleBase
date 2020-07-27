package com.swilkins.ScrabbleBase.Board;

import com.swilkins.ScrabbleBase.Board.State.Tile;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TileTests {

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
