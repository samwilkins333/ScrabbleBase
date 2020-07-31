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

public abstract class Trie implements Collection<String> {

  protected TrieNode root = new TrieNode(TrieNode.ROOT, null, false);
  private int size = 0;
  private int nodeSize = 0;

  private static final Predicate<String> DEFAULT_VALIDATOR = s -> true;
  protected Predicate<String> validator;

  protected Map<Character, Integer> manifestedAlphabet = new HashMap<>();

  public static final Predicate<String> LOWERCASE = s -> s.matches("^[a-z]+$");

  public Trie(Predicate<String> validator) {
    this.validator = validator != null ? validator : DEFAULT_VALIDATOR;
  }

  public Trie() {
    this.validator = DEFAULT_VALIDATOR;
  }

  public TrieNode getRoot() {
    return root;
  }

  public int getNodeSize() {
    return nodeSize;
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
      manifestedAlphabet.put(letter, manifestedAlphabet.getOrDefault(letter, 0) + 1);
    }
    return terminal;
  }

  protected void removeNodes(@NotNull char[] letters) {
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
      int newRefCount = manifestedAlphabet.getOrDefault(letter, 1) - 1;
      if (newRefCount > 0) {
        manifestedAlphabet.put(letter, newRefCount);
      } else {
        manifestedAlphabet.remove(letter);
      }
    }
  }

  protected List<String> collect() {
    List<String> collector = new ArrayList<>();
    traverseRecursive(root, "", collector);
    return collector;
  }

  private void traverseRecursive(TrieNode current, String accumulated, List<String> collector) {
    for (char letter : manifestedAlphabet.keySet()) {
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
  public final boolean add(String s) {
    if (s.isEmpty() || contains(s) || !validator.test(s)) {
      return false;
    }
    if (this.addImpl(s)) {
      size++;
      return true;
    }
    return false;
  }

  protected boolean addImpl(String s) {
    return this.addNodes(s.toCharArray());
  }

  @Override
  public final boolean remove(Object o) {
    if (!contains(o)) {
      return false;
    }
    this.removeImpl((String) o);
    size--;
    return true;
  }

  protected void removeImpl(String s) {
    this.removeNodes(s.toCharArray());
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
