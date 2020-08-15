package com.swilkins.ScrabbleBase.Board.State;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class Tile {

  private final char letter;
  private final int value;
  @JsonInclude(NON_NULL)
  private Character letterProxy;
  public static final char BLANK = '*';

  public Tile(char letter, int value, Character letterProxy) {
    this.letter = letter;
    this.value = value;
    this.letterProxy = letterProxy;
  }

  public char getLetter() {
    return letter;
  }

  public int getValue() {
    return value;
  }

  public Character getLetterProxy() {
    return letterProxy;
  }

  public void setLetterProxy(Character letterProxy) {
    this.letterProxy = letterProxy;
  }

  public char getResolvedLetter() {
    return this.letterProxy != null ? this.letterProxy : this.letter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tile tile = (Tile) o;
    boolean equals = letter == tile.letter &&
            value == tile.value;
    if (letterProxy == null && tile.letterProxy == null) {
      return equals;
    } else if (letterProxy != null && tile.letterProxy != null) {
      return equals && letterProxy.equals(tile.letterProxy);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(letter, value, letterProxy);
  }

}
