package com.swilkins.ScrabbleBase.Board;

import com.swilkins.ScrabbleBase.Board.State.Rack;
import org.junit.Before;
import org.junit.Test;

import static com.swilkins.ScrabbleBase.Board.Configuration.STANDARD_RACK_CAPACITY;
import static org.junit.Assert.*;

public class RackTests {
  private Rack rack;

  @Before
  public void initializeRack() {
    rack = new Rack(STANDARD_RACK_CAPACITY);
  }

  @Test
  public void isFullTest() {
    assertTrue(rack.isEmpty());

    rack.addAllFromLetters("abcdef");
    assertFalse(rack.isFull());
    assertEquals(6, rack.size());

    rack.addAllFromLetters("g");
    assertTrue(rack.isFull());
    assertEquals(STANDARD_RACK_CAPACITY, rack.size());
  }

  @Test
  public void addAllFromLettersOverflowTest() {
    rack.addAllFromLetters("abcdefgh");
    assertEquals(7, rack.size());
  }

  @Test
  public void addAllFromLettersNonEmptyOverflowTest() {
    rack.addAllFromLetters("abcd");
    assertEquals(4, rack.size());
    rack.addAllFromLetters("efghij");
    assertTrue(rack.isFull());
    assertEquals(STANDARD_RACK_CAPACITY, rack.size());
  }

}
