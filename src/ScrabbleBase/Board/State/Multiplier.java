package ScrabbleBase.Board.State;

import java.util.Objects;

public class Multiplier {
  private final int letter;
  private final int word;

  public Multiplier(int letter, int word) {
    this.letter = letter;
    this.word = word;
  }

  public Multiplier() {
    this.letter = 1;
    this.word = 1;
  }

  public int getLetterValue() {
    return letter;
  }

  public int getWordValue() {
    return word;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Multiplier that = (Multiplier) o;
    return that.letter == letter && that.word == word;
  }

  @Override
  public int hashCode() {
    return Objects.hash(letter, word);
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", letter, word);
  }

}
