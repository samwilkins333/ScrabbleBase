package com.swilkins.ScrabbleBase.Generation;

import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;

import java.util.Set;

/**
 * When <code>tryLetterPlacement()</code> verifies that placing a tile at a square
 * indeed forms a valid cross word, the <code>generate()</code> routine stores that computed cross
 * word along with the placed tile in an instance of this utility class. Since <code>generate()</code>
 * maintains a list of these enriched tile placements, it does not need to recompute cross words when
 * building a <code>Candidate</code> instance. Consider the following, where [Y] indicates a new placement:
 *
 *          T
 *          R
 * F I S H [Y]
 *
 * The <code>CrossedTilePlacement</code> that represents this placement is given by a single <code>TilePlacement</code>
 * for [Y] (representing the newly placed tile, or root), as well as a set of three <code>TilePlacement</code>s,
 * one for each of T, R and Y (representing the entire cross word formed by the new placement, including the new
 * placement itself).
 */
public class CrossedTilePlacement {

  // The newly placed tile
  private final TilePlacement root;
  // The cross word formed by the newly placed tile, including that new placement
  private final Set<TilePlacement> cross;

  public CrossedTilePlacement(TilePlacement root, Set<TilePlacement> cross) {
    this.root = root;
    this.cross = cross;
  }

  public TilePlacement getRoot() {
    return root;
  }

  public Set<TilePlacement> getCross() {
    return cross;
  }

}