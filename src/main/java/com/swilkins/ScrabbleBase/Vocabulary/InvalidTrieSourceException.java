package com.swilkins.ScrabbleBase.Vocabulary;

import java.net.URL;

public class InvalidTrieSourceException extends RuntimeException {

  public InvalidTrieSourceException(String message, URL dictionaryPath) {
    super(String.format("%s \"%s\"", message, dictionaryPath));
  }
}
