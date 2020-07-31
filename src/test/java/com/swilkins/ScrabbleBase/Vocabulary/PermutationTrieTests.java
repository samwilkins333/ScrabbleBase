package com.swilkins.ScrabbleBase.Vocabulary;

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

  @Test
  public void shouldCorrectlyAddAllWords() {
    PermutationTrie trie = new PermutationTrie(LOWERCASE);
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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldNotValidateNonsense() {
    PermutationTrie trie = new PermutationTrie(LOWERCASE);
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
    assertEquals(3, trie.size());
    assertEquals(3, trie.toArray().length);
    assertFalse(trie.isEmpty());

    trie.remove("world");

    assertEquals(2, trie.size());
    assertEquals(2, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertTrue(trie.contains("hello"));
    assertFalse(trie.contains("world"));
    assertTrue(trie.contains("worlds"));

    trie.add("world");
    trie.remove("worlds");

    assertEquals(2, trie.size());
    assertEquals(2, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertFalse(trie.contains("worlds"));

    trie.remove("hello");
    trie.remove("world");

    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
  }

  @Test
  public void clearingResetsSizeValues() {
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");
    assertEquals(3, trie.size());
    assertEquals(3, trie.toArray().length);
    assertFalse(trie.isEmpty());
    trie.clear();
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
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
    assertEquals(5, trie.size());
    assertEquals(5, trie.toArray().length);
    assertFalse(trie.isEmpty());
  }

  @Test
  public void shouldRejectInvalidWords() {
    trie = new PermutationTrie(LOWERCASE);
    assertFalse(trie.add("Uppercase"));
    assertFalse(trie.add("word3"));
    assertFalse(trie.add("special_chars.?>"));
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
  }

  @Test
  public void shouldAcceptAllWordsWithCustomValidator() {
    trie = new PermutationTrie(s -> true);
    assertTrue(trie.add("Uppercase"));
    assertTrue(trie.add("word3"));
    assertTrue(trie.add("special_chars.?>"));
    assertEquals(3, trie.size());
    assertEquals(3, trie.toArray().length);
    assertFalse(trie.isEmpty());
  }

  @Test
  public void shouldRejectWordsContainingDelimiter() {
    assertFalse(trie.add("hello#"));
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
    trie = new PermutationTrie(':');
    assertTrue(trie.add("hello#"));
    assertFalse(trie.add("hel:lo"));
    assertEquals(1, trie.size());
    assertEquals(1, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertFalse(trie.remove("hel:lo"));
    assertTrue(trie.remove("hello#"));
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
  }

  @Test
  public void shouldHandleWhitespaceAppropriately() {
    assertTrue(trie.add("hello  \nworld"));
    assertEquals(1, trie.size());
    assertEquals(1, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertTrue(trie.contains("hello  \nworld"));
    assertTrue(trie.add("normal"));
    assertEquals(2, trie.size());
    assertEquals(2, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertTrue(trie.remove("hello  \nworld"));
    assertEquals(1, trie.size());
    assertEquals(1, trie.toArray().length);
    assertFalse(trie.isEmpty());
    assertTrue(trie.remove("normal"));
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
    assertTrue(trie.isEmpty());
  }

}
