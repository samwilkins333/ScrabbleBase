package com.swilkins.ScrabbleBase.Vocabulary;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrieTests {

  @Test
  public void shouldCorrectlyAddAllWords() {
    URL url = TrieTests.class.getResource("/ospd4.txt");
    Trie trie = TrieFactory.loadFrom(url);
    try {
      assertTrue(new BufferedReader(new FileReader(url.getFile())).lines().allMatch(trie::includes));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldNotValidateNonsense() {
    Trie trie = TrieFactory.loadFrom(TrieTests.class.getResource("/ospd4.txt"));
    assertFalse(trie.includes(""));
    assertFalse(trie.includes("alsdkbhb"));
  }

}
