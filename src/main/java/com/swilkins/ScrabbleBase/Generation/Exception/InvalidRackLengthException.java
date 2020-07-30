package com.swilkins.ScrabbleBase.Generation.Exception;

public class InvalidRackLengthException extends IllegalArgumentException {

  public InvalidRackLengthException(int expectedLength, int actualLength) {
    super(String.format("The generator has been configured with a rack capacity of %d, but encountered a rack length of %d.", expectedLength, actualLength));
  }

}
