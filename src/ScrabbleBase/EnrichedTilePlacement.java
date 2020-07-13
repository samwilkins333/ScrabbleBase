package ScrabbleBase;

import java.util.List;

public class EnrichedTilePlacement {

  private final TilePlacement root;
  private final List<TilePlacement> cross;

  public EnrichedTilePlacement(TilePlacement root, List<TilePlacement> cross) {
    this.root = root;
    this.cross = cross;
  }

  public TilePlacement getRoot() {
    return root;
  }

  public List<TilePlacement> getCross() {
    return cross;
  }

}
