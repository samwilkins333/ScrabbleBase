package ScrabbleBase;

import java.util.Comparator;

public class Direction {

  public static final Direction UP = new Direction(0, -1, DirectionName.UP);
  public static final Direction DOWN = new Direction(0, 1, DirectionName.DOWN);
  public static final Direction LEFT = new Direction(-1, 0, DirectionName.LEFT);
  public static final Direction RIGHT = new Direction(1, 0, DirectionName.RIGHT);
  public static final Direction[] all = new Direction[] { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

  private final int xInc;
  private final int yInc;
  private final DirectionName name;

  public Direction(int xInc, int yInc, DirectionName name) {
    this.xInc = xInc;
    this.yInc = yInc;
    this.name = name;
  }

  public int nX(int x) {
    return x + this.xInc;
  }

  public int nY(int y) {
    return y + this.yInc;
  }

  public NextTile nextTile(int x, int y, BoardStateUnit[][] played) {
    Coordinates next;
    Tile tile;
    if ((next = this.nextCoordinates(x, y)) != null && (tile = played[next.getY()][next.getX()].getTile()) != null) {
      return new NextTile(next.getX(), next.getY(), tile);
    }
    return null;
  }

  public Coordinates nextCoordinates(int x, int y) {
    int nX = this.nX(x);
    int nY = this.nY(y);
    if (nX >= 0 && nX < 15 && nY >= 0 && nY < 15) {
      return new Coordinates(nX, nY);
    }
    return null;
  }

  public Direction inverse() {
    if (this == Direction.UP) {
      return Direction.DOWN;
    }
    if (this == Direction.DOWN) {
      return Direction.UP;
    }
    if (this == Direction.LEFT) {
      return Direction.RIGHT;
    }
    return Direction.LEFT;
  }

  public Direction perpendicular() {
    if (this == Direction.UP || this == Direction.DOWN) {
      return Direction.RIGHT;
    }
    return Direction.DOWN;
  }

  public Direction normalize() {
    if (this == Direction.UP || this == Direction.DOWN) {
      return Direction.DOWN;
    }
    return Direction.RIGHT;
  }

  public static Comparator<TilePlacement> along(Direction direction) {
    direction = direction.normalize();
    if (direction == Direction.DOWN) {
      return Comparator.comparing(TilePlacement::getY);
    }
    return Comparator.comparingInt(TilePlacement::getX);
  }

  public DirectionName name() {
    return this.name;
  }

}