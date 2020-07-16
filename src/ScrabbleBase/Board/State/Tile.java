package ScrabbleBase.Board.State;

public class Tile {

  private final char letter;
  private final int value;
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

}
