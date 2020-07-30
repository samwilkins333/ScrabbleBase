package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Generation.Exception.UnsetRackCapacityException;
import com.swilkins.ScrabbleBase.Generation.Exception.UnsetTrieException;
import com.swilkins.ScrabbleBase.Vocabulary.PermutationTrie;
import org.junit.Test;

import java.util.LinkedList;

import static com.swilkins.ScrabbleBase.Board.Configuration.STANDARD_RACK_CAPACITY;
import static com.swilkins.ScrabbleBase.Board.Configuration.getStandardBoard;
import static com.swilkins.ScrabbleBase.Generation.Generator.getDefaultOrdering;

@SuppressWarnings("ConstantConditions")
public class GenerationInvalidInputTests {
  private Generator generator;

  @Test(expected = IllegalArgumentException.class)
  public void supplyingNullTrieReferenceShouldThrow() {
    generator = new Generator(null, STANDARD_RACK_CAPACITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void settingNullTrieReferenceShouldThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.setTrie(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void supplyingNegativeRackCapacityShouldThrow() {
    generator = new Generator(new PermutationTrie(), -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void supplyingZeroRackCapacityShouldThrow() {
    generator = new Generator(new PermutationTrie(), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void settingNegativeRackCapacityShouldThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.setRackCapacity(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void settingZeroRackCapacityShouldThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.setRackCapacity(0);
  }

  @Test(expected = UnsetTrieException.class)
  public void unsetTrieGenerationShouldThrow() {
    generator = new Generator();
    generator.setRackCapacity(STANDARD_RACK_CAPACITY);
    generator.compute(new LinkedList<>(), getStandardBoard(), getDefaultOrdering());
  }

  @Test(expected = UnsetRackCapacityException.class)
  public void unsetRackCapacityGenerationShouldThrow() {
    generator = new Generator();
    generator.setTrie(new PermutationTrie());
    generator.compute(new LinkedList<>(), getStandardBoard(), getDefaultOrdering());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRackReferenceGenerationShouldThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.compute(null, getStandardBoard(), getDefaultOrdering());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullBoardReferenceGenerationShouldThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.compute(new LinkedList<>(), null, getDefaultOrdering());
  }

  @Test
  public void nullOrderingReferenceGenerationShouldNotThrow() {
    generator = new Generator(new PermutationTrie(), STANDARD_RACK_CAPACITY);
    generator.compute(new LinkedList<>(), getStandardBoard(), null);
  }

}
