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
      int quota = directImport.size();
      assertTrue(trie.containsAll(directImport));
      assertTrue(directImport.containsAll(trie));
      assertEquals(quota, trie.toArray().length);
      assertEquals(quota, trie.size());
      trie.retainAll(directImport);
      assertEquals(quota, trie.size());
      directImport.retainAll(trie);
      assertEquals(quota, directImport.size());
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
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");

    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertTrue(trie.contains("worlds"));
    assertNonZeroSize(trie, 3);

    trie.remove("world");

    assertNonZeroSize(trie, 2);
    assertTrue(trie.contains("hello"));
    assertFalse(trie.contains("world"));
    assertTrue(trie.contains("worlds"));

    trie.add("world");
    trie.remove("worlds");

    assertNonZeroSize(trie, 2);
    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertFalse(trie.contains("worlds"));

    trie.remove("hello");
    trie.remove("world");
    assertEmpty(trie);
  }

  @Test
  public void clearingResetsSizeValues() {
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");
    assertNonZeroSize(trie, 3);
    trie.clear();
    assertEmpty(trie);
  }

  @Test
  public void addingExistingWordsBehavesCorrectly() {
    assertTrue(trie.add("one"));
    assertTrue(trie.add("fish"));
    assertTrue(trie.add("two"));
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
    assertTrue(trie.add("Uppercase"));
    assertTrue(trie.add("word3"));
    assertTrue(trie.add("special_chars.?>"));
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
