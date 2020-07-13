package ScrabbleBase.Generation.Exception;

public class UnsetRackCapacityException extends RuntimeException {

  public UnsetRackCapacityException() {
    super("Cannot generate candidates without first setting a valid rack capacity value.");
  }
}
