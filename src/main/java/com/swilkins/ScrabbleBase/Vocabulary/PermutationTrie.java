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

  interface NodeMutator { boolean mutate(char[] letters); }

  @Override
  public boolean addImpl(String s) {
    if (s.contains(String.valueOf(delimiter))) {
      return false;
    }
    boolean result = executeWithPermutations(this::addNodes, s);
    this.manifestedAlphabet.remove(delimiter);
    return result;
  }

  @Override
  public boolean removeImpl(String s) {
    return executeWithPermutations(this::removeNodes, s);
  }

  private boolean executeWithPermutations(NodeMutator mutator, String s) {
    char[] letters = s.toCharArray();
    boolean result = mutator.mutate(letters);

    int count = letters.length;
    if (count > 1) {
      int currentIndex = count - 1;
      char[] variation = new char[count + 1];
      System.arraycopy(s.substring(1).toCharArray(), 0, variation, 0, currentIndex);
      variation[currentIndex] = delimiter;
      variation[count] = letters[0];
      while (currentIndex > 0) {
        result &= mutator.mutate(variation);
        variation[currentIndex--] = variation[0];
        if (currentIndex >= 0) System.arraycopy(variation, 1, variation, 0, currentIndex);
        variation[currentIndex] = delimiter;
      }
    }

    return result;
  }

}
