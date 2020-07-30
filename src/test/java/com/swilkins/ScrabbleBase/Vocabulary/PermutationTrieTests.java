package com.swilkins.ScrabbleBase.Vocabulary;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PermutationTrieTests {
  private PermutationTrie trie;

  @Before
  public void initializeTrie() {
    trie = new PermutationTrie();
  }

  @Test
  public void shouldCorrectlyAddAllWords() {
    URL url = PermutationTrieTests.class.getResource("/ospd4.txt");
    PermutationTrie trie = PermutationTrie.loadFrom(url);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(url.getFile()));
      Set<String> directImport = reader.lines().collect(Collectors.toSet());
      reader.close();
      int quota = directImport.size();
      assertTrue(trie.containsAll(directImport));
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
    PermutationTrie trie = PermutationTrie.loadFrom(PermutationTrieTests.class.getResource("/ospd4.txt"));
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

    trie.remove("world");

    assertEquals(2, trie.size());
    assertEquals(2, trie.toArray().length);
    assertTrue(trie.contains("hello"));
    assertFalse(trie.contains("world"));
    assertTrue(trie.contains("worlds"));

    trie.add("world");
    trie.remove("worlds");

    assertEquals(2, trie.size());
    assertEquals(2, trie.toArray().length);
    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertFalse(trie.contains("worlds"));

    trie.remove("hello");
    trie.remove("world");

    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
  }

  @Test
  public void clearingResetsSizeValues() {
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");
    assertEquals(3, trie.size());
    assertEquals(3, trie.toArray().length);
    trie.clear();
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
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
  }

  @Test
  public void shouldRejectInvalidWords() {
    assertFalse(trie.add("Uppercase"));
    assertFalse(trie.add("word3"));
    assertFalse(trie.add("special_chars.?>"));
    assertEquals(0, trie.size());
    assertEquals(0, trie.toArray().length);
    assertEquals(0, trie.getNodeSize());
  }

}
