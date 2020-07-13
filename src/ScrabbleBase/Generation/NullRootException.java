package ScrabbleBase.Generation;

public class NullRootException extends RuntimeException {

  public NullRootException() {
    super("Cannot generate candidates without first setting a valid Trie root.");
  }

}
