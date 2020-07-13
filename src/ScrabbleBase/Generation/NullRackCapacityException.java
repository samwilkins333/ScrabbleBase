package ScrabbleBase.Generation;

public class NullRackCapacityException extends RuntimeException {

  public NullRackCapacityException() {
    super("Cannot generate candidates without first setting a valid rack capacity value.");
  }
}
