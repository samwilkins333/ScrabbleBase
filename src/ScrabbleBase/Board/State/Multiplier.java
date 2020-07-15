package ScrabbleBase.Board.State;

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
  public String toString() {
    return String.format("(%d, %d)", letter, word);
  }

}
