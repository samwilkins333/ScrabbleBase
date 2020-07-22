package ScrabbleBase;

import ScrabbleBase.TestHarnesses.Board.Location.ScoredCandidateHashingAndEqualityTest;
import ScrabbleBase.TestHarnesses.Board.State.TileHashingAndEqualityTest;

import java.util.ArrayList;
import java.util.List;

public class RegressionTests {
  private static List<TestHarness> testHarnesses = new ArrayList<>();

  public static void main(String[] args) {
    testHarnesses.add(new ScoredCandidateHashingAndEqualityTest());
    testHarnesses.add(new TileHashingAndEqualityTest());

    System.out.println("\nRunning all regression tests...\n");
    System.out.printf("\n%s.\n", testHarnesses.stream().allMatch(TestHarness::run) ? "All tests passed" : "Some tests failed");
  }

}
