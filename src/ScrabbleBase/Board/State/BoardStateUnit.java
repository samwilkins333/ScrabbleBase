package ScrabbleBase.Board.State;

public class BoardStateUnit {

  private final Multiplier multiplier;
  private final Tile tile;

  public BoardStateUnit(Multiplier multiplier, Tile tile) {
    this.multiplier = multiplier;
    this.tile = tile;
  }

  public Multiplier getMultiplier() {
    return multiplier;
  }

  public Tile getTile() {
    return tile;
  }

}
