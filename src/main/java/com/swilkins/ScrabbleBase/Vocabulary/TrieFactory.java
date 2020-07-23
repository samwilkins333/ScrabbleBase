package com.swilkins.ScrabbleBase.Vocabulary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class TrieFactory {

  public static Trie loadFrom(URL dictionaryPath) throws InvalidTrieSourceException {
    Trie trie = new Trie();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath.getFile()));
      String word;
      while ((word = reader.readLine()) != null) {
        if (word.length() > 0) {
          trie.addWord(word.trim());
        }
      }
      return trie;
    } catch (FileNotFoundException e) {
      throw new InvalidTrieSourceException("Unable to locate dictionary file at", dictionaryPath);
    } catch (IOException e) {
      throw new InvalidTrieSourceException("Encountered error while reading from", dictionaryPath);
    }
  }

}
