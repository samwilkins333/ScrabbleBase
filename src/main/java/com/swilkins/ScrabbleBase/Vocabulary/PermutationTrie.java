package com.swilkins.ScrabbleBase.Vocabulary;

import java.util.function.Predicate;

public class PermutationTrie extends Trie {
  private static final char DEFAULT_DELIMITER = '#';
  protected char delimiter;

  public PermutationTrie(Predicate<String> validator, char delimiter) {
    super(validator);
    this.delimiter = delimiter;
  }

  public PermutationTrie(Predicate<String> validator) {
    super(validator);
    this.delimiter = DEFAULT_DELIMITER;
  }

  public PermutationTrie(char delimiter) {
    super(LOWERCASE);
    this.delimiter = delimiter;
  }

  public PermutationTrie() {
    super(LOWERCASE);
    this.delimiter = DEFAULT_DELIMITER;
  }

  public char getDelimiter() {
    return delimiter;
  }

  @Override
  public boolean addImpl(String s) {
    if (s.contains(String.valueOf(delimiter))) {
      return false;
    }
    char[] letters = s.toCharArray();
    boolean result = this.addNodes(letters);

    int count = letters.length;
    if (count > 1) {
      int currentIndex = count - 1;
      char[] variation = new char[count + 1];
      System.arraycopy(s.substring(1).toCharArray(), 0, variation, 0, currentIndex);
      variation[currentIndex] = delimiter;
      variation[count] = letters[0];
      while (currentIndex > 0) {
        result &= this.addNodes(variation);
        variation[currentIndex--] = variation[0];
        if (currentIndex >= 0) System.arraycopy(variation, 1, variation, 0, currentIndex);
        variation[currentIndex] = delimiter;
      }
    }
    this.manifestedAlphabet.remove(delimiter);
    return result;
  }

  @Override
  public void removeImpl(String s) {
    char[] letters = s.toCharArray();
    this.removeNodes(letters);

    int count = letters.length;
    if (count > 1) {
      int currentIndex = count - 1;
      char[] variation = new char[count + 1];
      System.arraycopy(s.substring(1).toCharArray(), 0, variation, 0, currentIndex);
      variation[currentIndex] = delimiter;
      variation[count] = letters[0];
      while (currentIndex > 0) {
        this.removeNodes(variation);
        variation[currentIndex--] = variation[0];
        if (currentIndex >= 0) System.arraycopy(variation, 1, variation, 0, currentIndex);
        variation[currentIndex] = delimiter;
      }
    }
  }

}
