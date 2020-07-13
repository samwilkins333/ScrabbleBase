package ScrabbleBase.Vocabulary;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

  public static final char ROOT = '@';
  private final char letter;
  private final TrieNode parent;
  private Boolean isTerminal;
  private Map<Character, TrieNode> children;

  public TrieNode(char letter, TrieNode parent, Boolean isTerminal) {
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

  public TrieNode getChild(char letter) {
    return this.children.get(letter);
  }

  public char getLetter() {
    return letter;
  }

  public TrieNode getParent() {
    return parent;
  }

  public Boolean getTerminal() {
    return isTerminal;
  }

  public void setTerminal(Boolean terminal) {
    isTerminal = terminal;
  }

}
