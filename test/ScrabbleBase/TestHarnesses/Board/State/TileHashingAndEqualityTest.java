package ScrabbleBase.TestHarnesses.Board.State;

import ScrabbleBase.Board.State.Tile;
import ScrabbleBase.TestHarness;

import java.util.HashSet;
import java.util.Set;

public class TileHashingAndEqualityTest extends TestHarness {
  private Tile first;
  private Tile second;

  public TileHashingAndEqualityTest() {
    super("TileHashingAndEquality");
  }

  @Override
  protected void prepare() {
    first = new Tile('a', 1, null);
    second = new Tile(Tile.BLANK, 0, null);
  }

  @Override
  protected boolean execute() {
    boolean passed = !first.equals(second);
    Tile firstDuplicate = new Tile('a', 1, null);
    Tile secondDuplicate = new Tile(Tile.BLANK, 0, null);
    passed &= firstDuplicate.equals(first) &&  secondDuplicate.equals(second);
    passed &= !new Tile(Tile.BLANK, 0, 'b').equals(second);
    Set<Tile> tiles = new HashSet<>();
    tiles.add(first);
    tiles.add(firstDuplicate);
    tiles.add(second);
    tiles.add(secondDuplicate);
    return passed && tiles.size() == 2;
  }

}
