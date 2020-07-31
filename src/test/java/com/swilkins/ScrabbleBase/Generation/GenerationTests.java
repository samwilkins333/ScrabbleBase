package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import com.swilkins.ScrabbleBase.Board.State.BoardSquare;
import com.swilkins.ScrabbleBase.Board.State.Rack;
import com.swilkins.ScrabbleBase.Board.State.Tile;
import com.swilkins.ScrabbleBase.Vocabulary.Alphabet;
import com.swilkins.ScrabbleBase.Vocabulary.PermutationTrie;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.swilkins.ScrabbleBase.Board.Configuration.*;
import static com.swilkins.ScrabbleBase.Generation.Generator.getDefaultOrdering;
import static com.swilkins.ScrabbleBase.Vocabulary.PermutationTrie.LOWERCASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenerationTests {
  private static Generator generator;
  private BoardSquare[][] board;
  private Rack rack;

  @BeforeClass
  public static void configureGenerator() {
    URL dictionary = GenerationTests.class.getResource("/ospd4.txt");
    PermutationTrie trie = new PermutationTrie(LOWERCASE);
    trie.loadFrom(dictionary, String::trim);
    generator = new Generator(trie, STANDARD_RACK_CAPACITY);
  }

  @Before
  public void configureGame() {
    board = getStandardBoard();
    rack = new Rack(STANDARD_RACK_CAPACITY);
  }

  @Test
  public void highestScoringRegularOpeningMoveTest() {
    rack.addAllFromLetters("aboride");

    List<Candidate> candidates = generator.compute(rack, board, getDefaultOrdering());

    assertEquals(1040, candidates.size());

    Candidate optimal = candidates.get(0);

    assertEquals(24, optimal.getScore());
    assertEquals(DirectionName.DOWN, optimal.getDirection());

    String expectedWord = "abider";
    int[] expectedX = new int[]{7, 7, 7, 7, 7, 7};
    int[] expectedY = new int[]{2, 3, 4, 5, 6, 7};

    for (int i = 0; i < optimal.getPrimary().size(); i++) {
      TilePlacement placement = optimal.getPrimary().get(i);
      assertEquals(expectedWord.charAt(i), placement.getTile().getLetter());
      assertEquals(expectedX[i], placement.getX());
      assertEquals(expectedY[i], placement.getY());
    }
  }

  @Test
  public void highestScoringBlankOpeningMoveTest() {
    rack.addAllFromLetters("sordma");

    rack.addFromLetter(Tile.BLANK);
    Candidate computedOptimal = generator.compute(rack, board, getDefaultOrdering()).get(0);
    rack.removeLast();

    LinkedList<Candidate> collector = new LinkedList<>();

    for (char letter : Alphabet.letters) {
      rack.add(new Tile(letter, 0, null));
      collector.add(generator.compute(rack, board, getDefaultOrdering()).get(0));
      rack.removeLast();
    }
    collector.sort(getDefaultOrdering());

    Candidate collectedOptimal = collector.get(0);
    assertEquals(computedOptimal.getPrimary().size(), collectedOptimal.getPrimary().size());
    assertEquals(computedOptimal.getDirection(), collectedOptimal.getDirection());
    assertEquals(computedOptimal.getScore(), collectedOptimal.getScore());
    for (int i = 0; i < computedOptimal.getPrimary().size(); i++) {
      TilePlacement computed = computedOptimal.getPrimary().get(i);
      TilePlacement collected = collectedOptimal.getPrimary().get(i);
      assertEquals(computed.getTile().getResolvedLetter(), collected.getTile().getResolvedLetter());
      assertEquals(computed.getX(), collected.getX());
      assertEquals(computed.getY(), collected.getY());
    }
  }

  @Test
  public void shouldNotFindAnyCandidates() {
    rack.addAllFromLetters("dczklmn");
    assertTrue(generator.compute(rack, board, getDefaultOrdering()).isEmpty());
  }

  @Test
  public void shouldFindCandidatesOnAllBorders() {
    board[7][7].setTile(getStandardTile('a'));
    board[7][8].setTile(getStandardTile('a'));

    rack.addAllFromLetters("fnhorsb");

    List<Candidate> candidates = generator.compute(rack, board, getDefaultOrdering());

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
            "haaf",
            "aah",
            "baa",
            "fan",
            "fan",
            "frosh",
            "frosh",
            "born",
            "ash",
            "boa",
    };
    boolean[][] expectedIsExisting = new boolean[][]{
            new boolean[]{false, false, false, false, false},
            new boolean[]{false, true, true, false},
            new boolean[]{true, true, false},
            new boolean[]{false, true, true},
            new boolean[]{false, true, false},
            new boolean[]{false, true, false},
            new boolean[]{false, false, false, false, false},
            new boolean[]{false, false, false, false, false},
            new boolean[]{false, false, false, false},
            new boolean[]{true, false, false},
            new boolean[]{false, false, true}
    };
    int[][] expectedX = new int[][]{
            new int[]{9, 9, 9, 9, 9},
            new int[]{6, 7, 8, 9},
            new int[]{7, 8, 9},
            new int[]{6, 7, 8},
            new int[]{7, 7, 7},
            new int[]{8, 8, 8},
            new int[]{8, 9, 10, 11, 12},
            new int[]{4, 5, 6, 7, 8},
            new int[]{6, 6, 6, 6},
            new int[]{7, 7, 7},
            new int[]{8, 8, 8},
    };
    int[][] expectedY = new int[][]{
            new int[]{5, 6, 7, 8, 9},
            new int[]{7, 7, 7, 7},
            new int[]{7, 7, 7},
            new int[]{7, 7, 7},
            new int[]{6, 7, 8},
            new int[]{6, 7, 8},
            new int[]{6, 6, 6, 6, 6},
            new int[]{8, 8, 8, 8, 8},
            new int[]{7, 8, 9, 10},
            new int[]{7, 8, 9},
            new int[]{5, 6, 7},
    };
    int[] expectedScore = new int[]{
            27,
            10,
            6,
            5,
            6,
            11,
            28,
            27,
            12,
            6,
            6,
    };

    for (int i = 0; i < expectedX.length; i++) {
      boolean matched = false;
      for (Candidate candidate : candidates) {
        List<TilePlacement> placements = candidate.getPrimary();
        if (placements.size() != expectedX[i].length) {
          continue;
        }
        int p;
        for (p = 0; p < placements.size(); p++) {
          TilePlacement placement = placements.get(p);
          if (placement.getX() != expectedX[i][p]) {
            break;
          }
          if (placement.getY() != expectedY[i][p]) {
            break;
          }
          if (placement.getTile().getResolvedLetter() != expectedWord[i].charAt(p)) {
            break;
          }
          if (placement.isExisting() != expectedIsExisting[i][p]) {
            break;
          }
        }
        if (p == placements.size()) {
          matched = true;
          assertEquals(expectedDirection[i].name(), candidate.getDirection());
          assertEquals(expectedScore[i], candidate.getScore());
          break;
        }
      }
      assertTrue(matched);
    }
  }

  @Test
  public void shouldFindCandidateSpanningGaps() {
    board[7][4].setTile(getStandardTile('s'));
    board[7][5].setTile(getStandardTile('o'));
    board[7][6].setTile(getStandardTile('o'));
    board[7][7].setTile(getStandardTile('n'));
    board[6][7].setTile(getStandardTile('i'));
    board[5][7].setTile(getStandardTile('o'));
    board[4][7].setTile(getStandardTile('c'));
    board[6][4].setTile(getStandardTile('e'));
    board[5][4].setTile(getStandardTile('l'));
    board[4][4].setTile(getStandardTile('a'));
    board[3][4].setTile(getStandardTile('h'));
    board[2][4].setTile(getStandardTile('s'));
    board[2][5].setTile(getStandardTile('t'));
    board[2][6].setTile(getStandardTile('e'));
    board[2][7].setTile(getStandardTile('a'));
    board[2][8].setTile(getStandardTile('l'));
    board[2][9].setTile(getStandardTile('i'));
    board[2][10].setTile(getStandardTile('n'));
    board[2][11].setTile(getStandardTile('g'));
    board[1][9].setTile(getStandardTile('l'));
    board[3][9].setTile(getStandardTile('a'));
    board[4][9].setTile(getStandardTile('r'));
    board[1][10].setTile(getStandardTile('i'));
    board[1][11].setTile(getStandardTile('a'));
    board[1][12].setTile(getStandardTile('r'));
    board[1][13].setTile(getStandardTile('s'));
    board[2][11].setTile(getStandardTile('g'));
    board[3][11].setTile(getStandardTile('r'));
    board[4][11].setTile(getStandardTile('e'));
    board[5][11].setTile(getStandardTile('e'));
    board[6][11].setTile(getStandardTile('s'));
    board[2][13].setTile(getStandardTile('e'));
    board[3][13].setTile(getStandardTile('e'));
    board[4][13].setTile(getStandardTile('s'));

    rack.addAllFromLetters("aenjpbz");

    List<Candidate> candidates = generator.compute(rack, board, getDefaultOrdering());

    assertEquals(330, candidates.size());

    String expectedWord = "careens";
    int[] expectedX = new int[]{7, 8, 9, 10, 11, 12, 13};
    int[] expectedY = new int[]{4, 4, 4, 4, 4, 4, 4};
    boolean[] expectedIsExisting = new boolean[]{true, false, true, false, true, false, true};
    DirectionName expectedDirection = Direction.RIGHT.name();
    int expectedScore = 18;

    boolean matched = false;
    for (Candidate candidate : candidates) {
      List<TilePlacement> placements = candidate.getPrimary();
      if (placements.size() != expectedX.length) {
        continue;
      }
      int p;
      for (p = 0; p < placements.size(); p++) {
        TilePlacement placement = placements.get(p);
        if (placement.getX() != expectedX[p]) {
          break;
        }
        if (placement.getY() != expectedY[p]) {
          break;
        }
        if (placement.getTile().getResolvedLetter() != expectedWord.charAt(p)) {
          break;
        }
        if (placement.isExisting() != expectedIsExisting[p]) {
          break;
        }
      }
      if (p == placements.size()) {
        matched = true;
        assertEquals(expectedDirection, candidate.getDirection());
        assertEquals(expectedScore, candidate.getScore());
        break;
      }
    }
    assertTrue(matched);
  }

}