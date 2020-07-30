package com.swilkins.ScrabbleBase.Generation.Exception;

public class UnsetTrieException extends RuntimeException {

  public UnsetTrieException() {
    super("Cannot generate candidates without first setting a valid Trie.");
  }

}
