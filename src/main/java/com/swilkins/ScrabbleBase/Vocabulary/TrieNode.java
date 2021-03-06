package com.swilkins.ScrabbleBase.Vocabulary;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

  public static final char ROOT = '@';
  private final char letter;
  private final TrieNode parent;
  private boolean isTerminal;
  private final Map<Character, TrieNode> children;

  public TrieNode(char letter, TrieNode parent, boolean isTerminal) {
    this.letter = letter;
    this.parent = parent;
    this.isTerminal = isTerminal;
    this.children = new HashMap<>();
  }

  public TrieNode addChild(char letter, boolean isTerminal) {
    TrieNode existing = this.children.get(letter);
    if (existing != null) {
      throw new Error(String.format("Attempted to add a duplicate child node: %s", letter));
    }
    TrieNode child = new TrieNode(letter, this, isTerminal);
    this.children.put(letter, child);
    return child;
  }

  public TrieNode removeChild(char letter) {
    return this.children.remove(letter);
  }

  public TrieNode getChild(char letter) {
    return this.children.get(letter);
  }

  public char getLetter() {
    return this.letter;
  }

  public TrieNode getParent() {
    return this.parent;
  }

  public boolean getTerminal() {
    return this.isTerminal;
  }

  public int getChildCount() {
    return this.children.size();
  }

  public void setTerminal(Boolean isTerminal) {
    this.isTerminal = isTerminal;
  }

}
