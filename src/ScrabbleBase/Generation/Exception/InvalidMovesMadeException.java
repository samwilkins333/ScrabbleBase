package ScrabbleBase.Generation.Exception;

public class InvalidMovesMadeException extends RuntimeException {

  public InvalidMovesMadeException() {
    super("Moves made must be a non-negative integer.");
  }
}
