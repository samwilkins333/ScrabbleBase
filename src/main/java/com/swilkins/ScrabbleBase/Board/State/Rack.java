package com.swilkins.ScrabbleBase.Board.State;

import java.util.LinkedList;

import static com.swilkins.ScrabbleBase.Board.Configuration.getStandardTile;

public class Rack extends LinkedList<Tile> {

  private final int capacity;

  public Rack(int capacity) {
    super();
    this.capacity = capacity;
  }

  public void addFromLetter(char letter) {
    if (this.size() == capacity) {
      return;
    }
    this.add(getStandardTile(letter));
  }

  @Override
  public boolean add(Tile tile) {
    if (this.size() == capacity) {
      return false;
    }
    return super.add(tile);
  }

  public void addAllFromLetters(char[] letters) {
    int i;
    for (i = 0; i < letters.length && size() < capacity; i++) {
      this.addFromLetter(letters[i]);
    }
  }

}
