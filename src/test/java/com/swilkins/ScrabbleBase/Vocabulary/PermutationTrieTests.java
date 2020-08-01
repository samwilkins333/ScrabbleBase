package com.swilkins.ScrabbleBase.Vocabulary;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import static com.swilkins.ScrabbleBase.Vocabulary.PermutationTrie.LOWERCASE;
import static org.junit.Assert.*;

public class PermutationTrieTests {
  private PermutationTrie trie;
  private static URL dictionary;

  @BeforeClass
  public static void resolveDictionary() {
    dictionary = PermutationTrieTests.class.getResource("/ospd4.txt");
  }

  @Before
  public void initializeTrie() {
    trie = new PermutationTrie();
  }

  private void assertEmpty(Trie trie) {
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
  }

  private void assertNonZeroSize(Trie trie, int size) {
    assertEquals(size, trie.size());
    assertEquals(size, trie.toArray().length);
    assertFalse(trie.isEmpty());
  }

  @Test
  public void shouldCorrectlyAddAllWords() {
    trieShouldCorrectlyAddAllWords(new PermutationTrie());
  }

  @Test
  public void superShouldCorrectlyAddAllWords() {
    trieShouldCorrectlyAddAllWords(new Trie(LOWERCASE));
  }

  private void trieShouldCorrectlyAddAllWords(Trie trie) {
    assertTrue(trie.loadFrom(dictionary, String::trim));
    assertFalse(trie.isEmpty());
    try {
      BufferedReader reader = new BufferedReader(new FileReader(dictionary.getFile()));
      Set<String> directImport = reader.lines().collect(Collectors.toSet());
      reader.close();
      assertNonZeroSize(trie, directImport.size());
      assertTrue(trie.containsAll(directImport));
      assertTrue(directImport.containsAll(trie));
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void shouldNotValidateNonsense() {
    assertTrue(trie.loadFrom(dictionary, String::trim));
    assertFalse(trie.contains(""));
    assertFalse(trie.contains("alsdkbhb"));
  }

  @Test
  public void removalShouldBehaveCorrectly() {
    assertTrue(trie.addAll("hello", "world", "worlds"));
    assertTrue(trie.containsAll("hello", "world", "worlds"));
    assertNonZeroSize(trie, 3);

    trie.remove("world");

    assertNonZeroSize(trie, 2);
    assertTrue(trie.containsAll("hello", "worlds"));
    assertFalse(trie.contains("world"));

    trie.add("world");
    trie.remove("worlds");

    assertNonZeroSize(trie, 2);
    assertTrue(trie.containsAll("hello", "world"));
    assertFalse(trie.contains("worlds"));

    assertTrue(trie.removeAll("hello", "world"));
    assertEmpty(trie);
  }

  @Test
  public void clearingResetsSizeValues() {
    assertTrue(trie.addAll("hello", "world", "worlds"));
    assertNonZeroSize(trie, 3);
    trie.clear();
    assertEmpty(trie);
  }

  @Test
  public void addingExistingWordsBehavesCorrectly() {
    assertTrue(trie.addAll("one", "fish", "two"));
    assertFalse(trie.add("fish"));
    assertTrue(trie.add("red"));
    assertFalse(trie.add("fish"));
    assertTrue(trie.add("blue"));
    assertFalse(trie.add("fish"));
    assertNonZeroSize(trie, 5);
  }

  @Test
  public void shouldRejectInvalidWords() {
    assertFalse(trie.add("Uppercase"));
    assertFalse(trie.add("word3"));
    assertFalse(trie.add("special_chars.?>"));
    assertEmpty(trie);
  }

  @Test
  public void shouldAcceptAllWordsWithCustomValidator() {
    trie = new PermutationTrie(s -> true);
    assertTrue(trie.addAll("Uppercase", "word3", "special_chars.?>"));
    assertNonZeroSize(trie, 3);
  }

  @Test
  public void shouldRejectWordsContainingDelimiter() {
    assertFalse(trie.add("hello#"));
    assertEmpty(trie);
    trie = new PermutationTrie(s -> true, ':');
    assertTrue(trie.add("hello#"));
    assertFalse(trie.add("hel:lo"));
    assertNonZeroSize(trie, 1);
    assertFalse(trie.remove("hel:lo"));
    assertTrue(trie.remove("hello#"));
    assertEmpty(trie);
  }

  @Test
  public void shouldHandleWhitespaceAppropriately() {
    trie = new PermutationTrie(s -> s.matches("^[a-z\\s]+$"));
    assertTrue(trie.add("hello  \nworld"));
    assertNonZeroSize(trie, 1);
    assertTrue(trie.contains("hello  \nworld"));
    assertTrue(trie.add("normal"));
    assertNonZeroSize(trie, 2);
    assertTrue(trie.remove("hello  \nworld"));
    assertNonZeroSize(trie, 1);
    assertTrue(trie.remove("normal"));
    assertEmpty(trie);
  }

}
