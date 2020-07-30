package com.swilkins.ScrabbleBase.Vocabulary;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import static org.junit.Assert.*;

public class PermutationTrieTests {

  @Test
  public void shouldCorrectlyAddAllWords() {
    URL url = PermutationTrieTests.class.getResource("/ospd4.txt");
    PermutationTrie trie = PermutationTrie.loadFrom(url);
    try {
      assertTrue(new BufferedReader(new FileReader(url.getFile())).lines().allMatch(trie::contains));
    } catch (FileNotFoundException e) {
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
    PermutationTrie trie = new PermutationTrie();
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");

    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertTrue(trie.contains("worlds"));
    assertEquals(3, trie.size());

    trie.remove("world");

    assertEquals(2, trie.size());
    assertTrue(trie.contains("hello"));
    assertFalse(trie.contains("world"));
    assertTrue(trie.contains("worlds"));

    trie.add("world");
    trie.remove("worlds");

    assertEquals(2, trie.size());
    assertTrue(trie.contains("hello"));
    assertTrue(trie.contains("world"));
    assertFalse(trie.contains("worlds"));

    trie.remove("hello");
    trie.remove("world");

    assertEquals(0, trie.size());
    assertEquals(0, trie.getNodeSize());
  }

  @Test
  public void clearingResetsSizeValues() {
    PermutationTrie trie = new PermutationTrie();
    trie.add("hello");
    trie.add("world");
    trie.add("worlds");
    assertEquals(3, trie.size());
    trie.clear();
    assertEquals(0, trie.size());
    assertEquals(0, trie.getNodeSize());
  }

  @Test
  public void addingExistingWordsBehavesCorrectly() {
    PermutationTrie trie = new PermutationTrie();
    assertTrue(trie.add("one"));
    assertTrue(trie.add("fish"));
    assertTrue(trie.add("two"));
    assertFalse(trie.add("fish"));
    assertTrue(trie.add("red"));
    assertFalse(trie.add("fish"));
    assertTrue(trie.add("blue"));
    assertFalse(trie.add("fish"));
  }

}
