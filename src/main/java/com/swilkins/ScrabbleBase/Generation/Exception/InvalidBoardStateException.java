package com.swilkins.ScrabbleBase.Generation.Exception;

public class InvalidBoardStateException extends IllegalArgumentException {

  public InvalidBoardStateException() {
    super("Boards must be continuously populated and represented by a grid of equal, odd dimensions no less than three.");
  }
}
