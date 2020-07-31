package com.swilkins.ScrabbleBase.Vocabulary;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PermutationTrie implements Collection<String> {

  private TrieNode root = new TrieNode(TrieNode.ROOT, null, false);
  private int size = 0;
  private int nodeSize = 0;

  private static final Predicate<String> DEFAULT_VALIDATOR = s -> true;
  private Predicate<String> validator;

  private static final char DEFAULT_DELIMITER = '#';
  private char delimiter;

  private Map<Character, Integer> manifestedAlphabet = new HashMap<>();

  public PermutationTrie(Predicate<String> validator, char delimiter) {
    this.validator = validator != null ? validator : DEFAULT_VALIDATOR;
    this.delimiter = delimiter;
  }

  public PermutationTrie(Predicate<String> validator) {
    this.validator = validator;
    this.delimiter = DEFAULT_DELIMITER;
  }

  public PermutationTrie(char delimiter) {
    this.validator = DEFAULT_VALIDATOR;
    this.delimiter = delimiter;
  }

  public PermutationTrie() {
    this.validator = DEFAULT_VALIDATOR;
    this.delimiter = DEFAULT_DELIMITER;
  }

  public TrieNode getRoot() {
    return root;
  }

  public int getNodeSize() {
    return nodeSize;
  }

  public char getDelimiter() {
    return delimiter;
  }

  private boolean addNodes(char[] letters) {
    boolean terminal = false;
    TrieNode node = this.root;
    TrieNode childNode;
    int count = letters.length;
    for (int i = 0; i < count; i++) {
      terminal = i + 1 == count;
      if ((childNode = node.getChild(letters[i])) == null) {
        node = node.addChild(letters[i], terminal);
        this.nodeSize++;
      } else {
        if ((terminal &= !childNode.getTerminal())) {
          childNode.setTerminal(true);
        }
        node = childNode;
      }
    }
    for (char letter : letters) {
      if (letter != delimiter) {
        manifestedAlphabet.put(letter, manifestedAlphabet.getOrDefault(letter, 0) + 1);
      }
    }
    return terminal;
  }

  private void removeNodes(@NotNull char[] letters) {
    TrieNode node = this.root;
    int i = 0;
    int count = letters.length;
    while (i < count) {
      node = node.getChild(letters[i++]);
    }
    if (node.getChildCount() > 0) {
      node.setTerminal(false);
    } else {
      TrieNode toRemove = node;
      do {
        TrieNode parent = toRemove.getParent();
        if (parent == null) {
          break;
        }
        parent.removeChild(toRemove.getLetter());
        nodeSize--;
        toRemove = parent;
      } while (toRemove.getChildCount() == 0 && !toRemove.getTerminal());
    }
    for (char letter : letters) {
      if (letter != delimiter) {
        int newRefCount = manifestedAlphabet.get(letter) - 1;
        if (newRefCount > 0) {
          manifestedAlphabet.put(letter, newRefCount);
        } else {
          manifestedAlphabet.remove(letter);
        }
      }
    }
  }

  public static PermutationTrie loadFrom(URL dictionaryPath) throws InvalidTrieSourceException {
    PermutationTrie trie = new PermutationTrie();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath.getFile()));
      String word;
      while ((word = reader.readLine()) != null) {
        word = word.trim();
        if (word.length() > 0) {
          trie.add(word);
        }
      }
      reader.close();
      return trie;
    } catch (FileNotFoundException | NullPointerException e) {
      throw new InvalidTrieSourceException("Unable to locate dictionary file at", dictionaryPath);
    } catch (IOException e) {
      throw new InvalidTrieSourceException("Encountered error while reading from", dictionaryPath);
    }
  }

  private List<String> collect() {
    List<String> collector = new ArrayList<>();
    traverseRecursive(root, "", collector);
    return collector;
  }

  private void traverseRecursive(TrieNode current, String accumulated, List<String> collector) {
    for (char letter : manifestedAlphabet.keySet()) {
      TrieNode child = current.getChild(letter);
      if (child != null) {
        if (child.getTerminal()) {
          collector.add(accumulated);
        }
        traverseRecursive(child, accumulated + letter, collector);
      }
    }
  }

  public void clear() {
    this.root = new TrieNode(TrieNode.ROOT, null, false);
    size = nodeSize = 0;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean contains(Object o) {
    String string = (String) o;
    if (string == null) {
      return false;
    }
    TrieNode node = this.root;
    char[] letters = string.toCharArray();
    int count = string.length();

    int i;
    TrieNode childNode;
    for (i = 0; i < count; i++) {
      if ((childNode = node.getChild(letters[i])) == null) {
        break;
      }
      node = childNode;
    }
    return i == count && node.getTerminal();
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return collect().iterator();
  }

  @Override
  public void forEach(Consumer<? super String> action) {
    collect().forEach(action);
  }

  @NotNull
  @Override
  public Object[] toArray() {
    return collect().toArray();
  }

  @NotNull
  @Override
  @SuppressWarnings("SuspiciousToArrayCall")
  public <T> T[] toArray(@NotNull T[] a) {
    return collect().toArray(a);
  }

  @Override
  public boolean add(String s) {
    if (contains(s) || !validator.test(s) || s.contains(String.valueOf(delimiter))) {
      return false;
    }
    char[] letters = s.toCharArray();
    if (this.addNodes(letters)) {
      this.size++;
    }

    int count = letters.length;
    if (count > 1) {
      int currentIndex = count - 1;
      char[] variation = new char[count + 1];
      System.arraycopy(s.substring(1).toCharArray(), 0, variation, 0, currentIndex);
      variation[currentIndex] = delimiter;
      variation[count] = letters[0];
      while (currentIndex > 0) {
        this.addNodes(variation);
        variation[currentIndex--] = variation[0];
        if (currentIndex >= 0) System.arraycopy(variation, 1, variation, 0, currentIndex);
        variation[currentIndex] = delimiter;
      }
    }
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (!contains(o)) {
      return false;
    }
    String s = (String) o;
    char[] letters = s.toCharArray();
    this.removeNodes(letters);
    size--;

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
    return true;
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    return c.stream().allMatch(this::contains);
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends String> c) {
    return c.stream().allMatch(this::add);
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    return c.stream().allMatch(this::remove);
  }

  @Override
  public boolean removeIf(Predicate<? super String> filter) {
    return collect().stream().filter(filter).allMatch(this::remove);
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    return collect().stream().filter(s -> !c.contains(s)).allMatch(this::remove);
  }

  @Override
  public Spliterator<String> spliterator() {
    return collect().spliterator();
  }

  @Override
  public Stream<String> stream() {
    return collect().stream();
  }

  @Override
  public Stream<String> parallelStream() {
    return collect().parallelStream();
  }

}
