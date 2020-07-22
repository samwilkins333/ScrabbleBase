package ScrabbleBase.Board.Location;

import ScrabbleBase.Board.State.Tile;

import java.util.Objects;

public class TilePlacement {

  private final int x;
  private final int y;
  private final Tile tile;

  public TilePlacement(int x, int y, Tile tile) {
    this.x = x;
    this.y = y;
    this.tile = tile;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Tile getTile() {
    return tile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TilePlacement placement = (TilePlacement) o;
    return x == placement.x &&
            y == placement.y &&
            tile.equals(placement.tile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, tile);
  }

}
