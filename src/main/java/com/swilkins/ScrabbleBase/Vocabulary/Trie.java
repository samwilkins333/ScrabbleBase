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

public class Trie implements Collection<String> {

  protected TrieNode root = new TrieNode(TrieNode.ROOT, null, false);
  private int size = 0;
  private int nodeSize = 0;

  private static final Predicate<String> DEFAULT_VALIDATOR = s -> true;
  protected Predicate<String> validator;

  protected Map<Character, Integer> alphabet = new HashMap<>();

  public static final Predicate<String> LOWERCASE = s -> s.matches("^[a-z]+$");

  public Trie(Predicate<String> validator) {
    this.validator = validator != null ? validator : DEFAULT_VALIDATOR;
  }

  public Trie() {
    this.validator = DEFAULT_VALIDATOR;
  }

  public TrieNode getRoot() {
    return this.root;
  }

  public int getNodeSize() {
    return this.nodeSize;
  }

  public Set<Character> getAlphabet() {
    return this.alphabet.keySet();
  }

  public boolean loadFrom(URL dictionaryPath, InputTransformer transformer) throws InvalidTrieSourceException {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath.getFile()));
      Stream<String> input = reader.lines().distinct();
      if (transformer != null) {
        input = input.map(transformer::transform);
      }
      boolean result = input.allMatch(this::add);
      reader.close();
      return result;
    } catch (FileNotFoundException | NullPointerException e) {
      throw new InvalidTrieSourceException("Unable to locate dictionary file at", dictionaryPath);
    } catch (IOException e) {
      throw new InvalidTrieSourceException("Encountered error while reading from", dictionaryPath);
    }
  }

  protected boolean addNodes(char[] letters) {
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
      this.alphabet.put(letter, this.alphabet.getOrDefault(letter, 0) + 1);
    }
    return terminal;
  }

  protected boolean removeNodes(@NotNull char[] letters) {
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
        this.nodeSize--;
        toRemove = parent;
      } while (toRemove.getChildCount() == 0 && !toRemove.getTerminal());
    }
    for (char letter : letters) {
      int newRefCount = this.alphabet.getOrDefault(letter, 1) - 1;
      if (newRefCount > 0) {
        this.alphabet.put(letter, newRefCount);
      } else {
        this.alphabet.remove(letter);
      }
    }
    return true;
  }

  protected List<String> collect() {
    List<String> collector = new ArrayList<>();
    traverseRecursive(this.root, "", collector);
    return collector;
  }

  private void traverseRecursive(TrieNode current, String accumulated, List<String> collector) {
    for (char letter : this.getAlphabet()) {
      TrieNode child = current.getChild(letter);
      if (child != null) {
        String updated = accumulated + letter;
        if (child.getTerminal()) {
          collector.add(updated);
        }
        traverseRecursive(child, updated, collector);
      }
    }
  }

  public void clear() {
    this.root = new TrieNode(TrieNode.ROOT, null, false);
    this.size = this.nodeSize = 0;
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
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
    return this.collect().iterator();
  }

  @Override
  public void forEach(Consumer<? super String> action) {
    this.collect().forEach(action);
  }

  @NotNull
  @Override
  public Object[] toArray() {
    return this.collect().toArray();
  }

  @NotNull
  @Override
  @SuppressWarnings("SuspiciousToArrayCall")
  public <T> T[] toArray(@NotNull T[] a) {
    return this.collect().toArray(a);
  }

  @Override
  public final boolean add(String s) {
    if (s.isEmpty() || contains(s) || !this.validator.test(s)) {
      return false;
    }
    if (this.addImpl(s)) {
      this.size++;
      return true;
    }
    return false;
  }

  protected boolean addImpl(String s) {
    return this.addNodes(s.toCharArray());
  }

  @Override
  public final boolean remove(Object o) {
    if (!this.contains(o)) {
      return false;
    }
    if (this.removeImpl((String) o)) {
      this.size--;
      return true;
    }
    return false;
  }

  protected boolean removeImpl(String s) {
    this.removeNodes(s.toCharArray());
    return true;
  }

  @Override
  public final boolean containsAll(@NotNull Collection<?> c) {
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
    return this.collect().stream().filter(filter).allMatch(this::remove);
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    return this.collect().stream().filter(s -> !c.contains(s)).allMatch(this::remove);
  }

  @Override
  public Spliterator<String> spliterator() {
    return this.collect().spliterator();
  }

  @Override
  public Stream<String> stream() {
    return this.collect().stream();
  }

  @Override
  public Stream<String> parallelStream() {
    return this.collect().parallelStream();
  }

}
