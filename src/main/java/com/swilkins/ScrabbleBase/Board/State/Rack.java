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

  public void addAllFromLetters(String letters) {
    char[] exploded = letters.toCharArray();
    int i;
    for (i = 0; i < exploded.length && size() < capacity; i++) {
      this.addFromLetter(exploded[i]);
    }
  }

  public boolean isFull() {
    return capacity == size();
  }

}
