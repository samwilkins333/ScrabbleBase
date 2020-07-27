package com.swilkins.ScrabbleBase.Board;

import com.swilkins.ScrabbleBase.Board.State.BoardSquare;
import com.swilkins.ScrabbleBase.Board.State.Multiplier;
import com.swilkins.ScrabbleBase.Board.State.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Configuration {

  public static final int STANDARD_RACK_CAPACITY = 7;
  public static final int STANDARD_BOARD_DIMENSIONS = 15;
  public static final int STANDARD_BINGO = 50;

  public static final Map<Character, TileConfiguration> standardTileMapping = new HashMap<>();

  static {
    standardTileMapping.put(Tile.BLANK, new TileConfiguration(2, 0));
    standardTileMapping.put('a', new TileConfiguration(9, 1));
    standardTileMapping.put('b', new TileConfiguration(2, 3));
    standardTileMapping.put('c', new TileConfiguration(2, 3));
    standardTileMapping.put('d', new TileConfiguration(4, 2));
    standardTileMapping.put('e', new TileConfiguration(12, 1));
    standardTileMapping.put('f', new TileConfiguration(2, 4));
    standardTileMapping.put('g', new TileConfiguration(3, 2));
    standardTileMapping.put('h', new TileConfiguration(2, 4));
    standardTileMapping.put('i', new TileConfiguration(9, 1));
    standardTileMapping.put('j', new TileConfiguration(1, 8));
    standardTileMapping.put('k', new TileConfiguration(1, 5));
    standardTileMapping.put('l', new TileConfiguration(4, 1));
    standardTileMapping.put('m', new TileConfiguration(2, 3));
    standardTileMapping.put('n', new TileConfiguration(6, 1));
    standardTileMapping.put('o', new TileConfiguration(8, 1));
    standardTileMapping.put('p', new TileConfiguration(2, 3));
    standardTileMapping.put('q', new TileConfiguration(1, 10));
    standardTileMapping.put('r', new TileConfiguration(6, 1));
    standardTileMapping.put('s', new TileConfiguration(4, 1));
    standardTileMapping.put('t', new TileConfiguration(6, 1));
    standardTileMapping.put('u', new TileConfiguration(4, 1));
    standardTileMapping.put('v', new TileConfiguration(2, 4));
    standardTileMapping.put('w', new TileConfiguration(2, 4));
    standardTileMapping.put('x', new TileConfiguration(1, 8));
    standardTileMapping.put('y', new TileConfiguration(2, 4));
    standardTileMapping.put('z', new TileConfiguration(1, 10));
  }

  public static void logBoard(BoardSquare[][] board) {
    int d = board.length;
    List<String> indices = new ArrayList<>();
    indices.add(" ");
    for (int x = 0; x < d; x++) {
      indices.add(String.valueOf(x % 10));
    }
    System.out.println(String.join(" ", indices));
    for (int y = 0; y < d; y++) {
      List<String> letters = new ArrayList<>();
      letters.add(String.valueOf(y % 10));
      for (int x = 0; x < d; x++) {
        Tile played = board[y][x].getTile();
        letters.add(played != null ? String.valueOf(played.getResolvedLetter()) : "_");
      }
      System.out.println(String.join(" ", letters));
    }
  }

  public static List<Tile> getStandardTileBag() {
    List<Tile> tileBag = new ArrayList<>();
    for (Map.Entry<Character, TileConfiguration> entry : standardTileMapping.entrySet()) {
      for (int i = 0; i < entry.getValue().getFrequency(); i++) {
        tileBag.add(new Tile(entry.getKey(), entry.getValue().getValue(), null));
      }
    }
    return tileBag;
  }

  public static Tile getStandardTile(char letter) {
    TileConfiguration configuration = standardTileMapping.get(letter);
    if (configuration == null) {
      return null;
    }
    return new Tile(letter, configuration.getValue(), null);
  }

  public static final class TileConfiguration {

    private final int frequency;
    private final int value;

    public TileConfiguration(int frequency, int value) {
      this.frequency = frequency;
      this.value = value;
    }

    public int getFrequency() {
      return frequency;
    }

    public int getValue() {
      return value;
    }

  }

  public static BoardSquare[][] getStandardBoard() {
    int d = STANDARD_BOARD_DIMENSIONS;
    BoardSquare[][] board = new BoardSquare[d][d];
    for (int y = 0; y < d; y++) {
      for (int x = 0; x < d; x++) {
        int _x = Math.min(x, d - 1 - x);
        int _y = Math.min(y, d - 1 - y);
        Multiplier special = Configuration.standardMultiplierMapping.get(_y).get(_x);
        Multiplier resolved = special != null ? special : new Multiplier();
        board[y][x] = new BoardSquare(resolved, null);
      }
    }
    return board;
  }

  public static final List<Map<Integer, Multiplier>> standardMultiplierMapping = new ArrayList<>();

  static {
    Map<Integer, Multiplier> local = new HashMap<>();
    local.put(0, new Multiplier(1, 3));
    local.put(3, new Multiplier(2, 1));
    local.put(7, new Multiplier(1, 3));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(1, new Multiplier(1, 2));
    local.put(5, new Multiplier(3, 1));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(2, new Multiplier(1, 2));
    local.put(6, new Multiplier(2, 1));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(0, new Multiplier(2, 1));
    local.put(3, new Multiplier(1, 2));
    local.put(7, new Multiplier(2, 1));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(4, new Multiplier(1, 2));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(1, new Multiplier(3, 1));
    local.put(5, new Multiplier(3, 1));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(2, new Multiplier(2, 1));
    local.put(6, new Multiplier(2, 1));
    standardMultiplierMapping.add(local);

    local = new HashMap<>();
    local.put(0, new Multiplier(1, 3));
    local.put(3, new Multiplier(2, 1));
    local.put(7, new Multiplier(1, 2));
    standardMultiplierMapping.add(local);
  }

}
