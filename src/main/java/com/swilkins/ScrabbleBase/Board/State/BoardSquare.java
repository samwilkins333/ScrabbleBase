package com.swilkins.ScrabbleBase.Board.State;

public class BoardSquare {

  private final Multiplier multiplier;
  private Tile tile;

  public BoardSquare(Multiplier multiplier, Tile tile) {
    this.multiplier = multiplier;
    this.tile = tile;
  }

  public Multiplier getMultiplier() {
    return multiplier;
  }

  public Tile getTile() {
    return tile;
  }

  public void setTile(Tile tile) {
    this.tile = tile;
  }

}
