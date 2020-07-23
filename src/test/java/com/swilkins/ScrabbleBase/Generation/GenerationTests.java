package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardStateUnit;
import com.swilkins.ScrabbleBase.Board.State.Rack;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Generation.Direction.Direction;
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
  private Rack rack;

  @BeforeClass
  public static void configureGenerator() {
    Generator.setRackCapacity(STANDARD_RACK_CAPACITY);
    URL dictionaryPath = GenerationTests.class.getResource("/ospd4.txt");
    Generator.setRoot(TrieFactory.loadFrom(dictionaryPath).getRoot());
  }

  @Before
  public void configureGame() {
    board = getStandardBoard();
    rack = new Rack(STANDARD_RACK_CAPACITY);
  }

  @Test
  public void highestScoringRegularOpeningMoveTest() {
    rack.addAllFromLetters("aboride");

    List<ScoredCandidate> candidates = Generator.computeAllCandidates(rack, board);

    assertEquals(1040, candidates.size());

    ScoredCandidate optimal = candidates.get(0);

    assertEquals(24, optimal.getScore());
    assertEquals(DirectionName.DOWN, optimal.getDirection());

    String expectedWord = "abider";
    int[] expectedX = new int[]{7, 7, 7, 7, 7, 7};
    int[] expectedY = new int[]{2, 3, 4, 5, 6, 7};

    for (int i = 0; i < optimal.getPlacements().size(); i++) {
      TilePlacement placement = optimal.getPlacements().get(i);
      assertEquals(expectedWord.charAt(i), placement.getTile().getLetter());
      assertEquals(expectedX[i], placement.getX());
      assertEquals(expectedY[i], placement.getY());
    }
  }

  @Test
  public void highestScoringBlankOpeningMoveTest() {
    rack.addAllFromLetters("sordma");

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
    rack.addAllFromLetters("dczklmn");
    assertTrue(Generator.computeAllCandidates(rack, board).isEmpty());
  }

  @Test
  public void shouldFindCandidatesOnAllBordersRegular() {
    board[7][7].setTile(getStandardTile('a'));
    board[7][8].setTile(getStandardTile('a'));

    rack.addAllFromLetters("fnhorsb");

    List<ScoredCandidate> candidates = Generator.computeAllCandidates(rack, board);

    assertEquals(444, candidates.size());

    Direction[] expectedDirection = new Direction[]{
            Direction.DOWN,
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.DOWN,
            Direction.DOWN,
            Direction.RIGHT,
            Direction.RIGHT,
            Direction.DOWN,
            Direction.DOWN,
            Direction.DOWN,
    };
    String[] expectedWord = new String[]{
            "fohns",
            "hf",
            "h",
            "b",
            "fn",
            "fn",
            "frosh",
            "frosh",
            "born",
            "sh",
            "bo",
    };
    int[][] expectedX = new int[][]{
            new int[]{9, 9, 9, 9, 9},
            new int[]{6, 9},
            new int[]{9},
            new int[]{6},
            new int[]{7, 7},
            new int[]{8, 8},
            new int[]{8, 9, 10, 11, 12},
            new int[]{4, 5, 6, 7, 8},
            new int[]{6, 6, 6, 6},
            new int[]{7, 7},
            new int[]{8, 8},
    };
    int[][] expectedY = new int[][]{
            new int[]{5, 6, 7, 8, 9},
            new int[]{7, 7},
            new int[]{7},
            new int[]{7},
            new int[]{6, 8},
            new int[]{6, 8},
            new int[]{6, 6, 6, 6, 6},
            new int[]{8, 8, 8, 8, 8},
            new int[]{7, 8, 9, 10},
            new int[]{8, 9},
            new int[]{5, 6},
    };

    for (int i = 0; i < expectedX.length; i++) {
      boolean matched = false;
      for (ScoredCandidate candidate : candidates) {
        List<TilePlacement> placements = candidate.getPlacements();
        if (placements.size() != expectedX[i].length) {
          continue;
        }
        int p;
        for (p = 0; p < placements.size(); p++) {
          if (candidate.getPlacements().get(p).getX() != expectedX[i][p]) {
            break;
          }
          if (candidate.getPlacements().get(p).getY() != expectedY[i][p]) {
            break;
          }
          if (candidate.getPlacements().get(p).getTile().getResolvedLetter() != expectedWord[i].charAt(p)) {
            break;
          }
        }
        if (p == placements.size()) {
          matched = true;
          assertEquals(expectedDirection[i].name(), candidate.getDirection());
          break;
        }
      }
      assertTrue(matched);
    }
  }

}