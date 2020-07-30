package com.swilkins.ScrabbleBase.Board.Location;

import java.util.Objects;

public class Coordinates {

  private final int x;
  private final int y;

  public Coordinates(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Coordinates that = (Coordinates) o;
    return that.x == x && that.y == y;
  }

  @Override
  public String toString() {
    return String.format("(%s, %s)", x, y);
  }

}
