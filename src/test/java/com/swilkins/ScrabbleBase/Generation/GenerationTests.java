package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardStateUnit;
import com.swilkins.ScrabbleBase.Board.State.Rack;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.DirectionName;
import com.swilkins.ScrabbleBase.Generation.Objects.ScoredCandidate;
import com.swilkins.ScrabbleBase.Vocabulary.Alphabet;
import com.swilkins.ScrabbleBase.Vocabulary.TrieFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.swilkins.ScrabbleBase.Board.Configuration.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenerationTests {
  private BoardStateUnit[][] board;

  @BeforeClass
  public static void configureGenerator() {
    Generator.setRackCapacity(STANDARD_RACK_CAPACITY);
    URL dictionaryPath = GenerationTests.class.getResource("/ospd4.txt");
    Generator.setRoot(TrieFactory.loadFrom(dictionaryPath).getRoot());
  }

  @Before
  public void configureBoard() {
    board = getStandardBoard();
  }

  @Test
  public void highestScoringRegularOpeningMoveTest() {
    Rack rack = new Rack(STANDARD_RACK_CAPACITY);

    rack.addAllFromLetters(new char[]{'a', 'b', 'o', 'r', 'i', 'd', 'e'});

    List<ScoredCandidate> candidates = Generator.computeAllCandidates(rack, board);

    assertEquals(1040, candidates.size());

    ScoredCandidate optimal = candidates.get(0);

    assertEquals(24, optimal.getScore());
    assertEquals(DirectionName.DOWN, optimal.getDirection());

    int[] expectedY = new int[]{2, 3, 4, 5, 6, 7};
    int[] expectedX = new int[]{7, 7, 7, 7, 7, 7};
    char[] expectedLetter = new char[]{'a', 'b', 'i', 'd', 'e', 'r'};

    for (int i = 0; i < optimal.getPlacements().size(); i++) {
      TilePlacement placement = optimal.getPlacements().get(i);
      assertEquals(expectedLetter[i], placement.getTile().getLetter());
      assertEquals(expectedX[i], placement.getX());
      assertEquals(expectedY[i], placement.getY());
    }
  }

  @Test
  public void highestScoringBlankOpeningMoveTest() {
    Rack rack = new Rack(STANDARD_RACK_CAPACITY);

    rack.addAllFromLetters(new char[]{'s', 'o', 'r', 'd', 'm', 'a'});

    rack.addFromLetter(Tile.BLANK);
    ScoredCandidate computedOptimal = Generator.computeAllCandidates(rack, board).get(0);
    rack.removeLast();

    LinkedList<ScoredCandidate> collector = new LinkedList<>();

    for (char letter : Alphabet.letters) {
      rack.add(new Tile(letter, 0, null));
      collector.add(Generator.computeAllCandidates(rack, board).get(0));
      rack.removeLast();
    }
    collector.sort(Generator.getDefaultOrdering());

    ScoredCandidate collectedOptimal = collector.get(0);
    assertEquals(computedOptimal.getPlacements().size(), collectedOptimal.getPlacements().size());
    assertEquals(computedOptimal.getDirection(), collectedOptimal.getDirection());
    assertEquals(computedOptimal.getScore(), collectedOptimal.getScore());
    for (int i = 0; i < computedOptimal.getPlacements().size(); i++) {
      TilePlacement computed = computedOptimal.getPlacements().get(i);
      TilePlacement collected = collectedOptimal.getPlacements().get(i);
      assertEquals(computed.getTile().getResolvedLetter(), collected.getTile().getResolvedLetter());
      assertEquals(computed.getX(), collected.getX());
      assertEquals(computed.getY(), collected.getY());
    }
  }

  @Test
  public void shouldNotFindAnyCandidates() {
    Rack rack = new Rack(STANDARD_RACK_CAPACITY);

    rack.addAllFromLetters(new char[]{'d', 'c', 'z', 'k', 'l', 'm', 'n'});

    assertTrue(Generator.computeAllCandidates(rack, board).isEmpty());
  }

}