package ScrabbleBase.Board.State;

import java.util.Objects;

/**
 * Models a multiplier for a scrabble <code>Board</code> square.
 * Consists of a multiplier for both the given raw and the overall
 * given word.
 */
public class Multiplier {
  private final int letter;
  private final int word;

  /**
   * Constructor.
   * @param letter the numeric raw multiplier
   * @param word the numeric word multiplier
   */
  public Multiplier(int letter, int word) {
    this.letter = letter;
    this.word = word;
  }

  public Multiplier() {
    this.letter = 1;
    this.word = 1;
  }

  /**
   * A utility constructor that is used to split
   * two numbers along the specified delimiter
   * and create a <code>Multiplier</code> using
   * the two values.
   * @param raw the String to parse
   * @param delimiter the delimiter separating
   *                  the two numbers
   * @return an <code>Multiplier</code> whose raw multiplier
   * is the first number, and whose word is the second
   */
  public static Multiplier parse(String raw, String delimiter) {
    String[] split = raw.split(delimiter);
    int letterValue = Integer.parseInt(split[0]);
    int wordValue = Integer.parseInt(split[1]);
    return new Multiplier(letterValue, wordValue);
  }

  /**
   * @return the numeric raw multiplier
   */
  public int getLetterValue() {
    return letter;
  }

  /**
   * @return the numeric word multiplier
   */
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
    return String.format("Multiplier: Letter [%d], Word [%d]", letter, word);
  }
}
