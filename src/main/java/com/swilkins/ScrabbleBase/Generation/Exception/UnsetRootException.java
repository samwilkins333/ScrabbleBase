package com.swilkins.ScrabbleBase.Generation.Exception;

public class UnsetRootException extends RuntimeException {

  public UnsetRootException() {
    super("Cannot generate candidates without first setting a valid Trie root.");
  }

}
