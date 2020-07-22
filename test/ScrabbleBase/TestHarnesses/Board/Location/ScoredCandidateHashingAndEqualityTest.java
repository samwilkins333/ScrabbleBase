package ScrabbleBase.TestHarnesses.Board.Location;

import ScrabbleBase.Board.Location.TilePlacement;
import ScrabbleBase.Board.State.Tile;
import ScrabbleBase.Generation.Direction.DirectionName;
import ScrabbleBase.Generation.Objects.ScoredCandidate;
import ScrabbleBase.TestHarness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoredCandidateHashingAndEqualityTest extends TestHarness {
  private ScoredCandidate first;
  private ScoredCandidate second;

  public ScoredCandidateHashingAndEqualityTest() {
    super("ScoredCandidateHashingAndEquality");
  }

  @Override
  protected void prepare() {
    List<TilePlacement> firstPlacements = new ArrayList<>();
    List<TilePlacement> secondPlacements = new ArrayList<>();
    char[] letters = { 'a', 'u', Tile.BLANK, 's', 'e', 'r', 'o' };
    int[] values = { 1, 1, 0, 1, 1, 1, 1 };
    int score = 0;
    for (int i = 0; i < letters.length; i++) {
      char letter = letters[i];
      int value = values[i];
      score += value;
      firstPlacements.add(new TilePlacement(i, 7, new Tile(letter, value, null)));
      secondPlacements.add(new TilePlacement(i, 7, new Tile(letter, value, null)));
    }
    first = new ScoredCandidate(firstPlacements, DirectionName.RIGHT, score);
    second = new ScoredCandidate(secondPlacements, DirectionName.RIGHT, score);
  }

  @Override
  protected boolean execute() {
    Set<ScoredCandidate> collector = new HashSet<>();
    collector.add(first);
    collector.add(second);
    return first.equals(second) && collector.size() == 1;
  }

}
