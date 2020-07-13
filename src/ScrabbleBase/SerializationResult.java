package ScrabbleBase;

public class SerializationResult {

  private final String serialized;
  private final DirectionName normalized;

  public SerializationResult(String serialized, DirectionName normalized) {
    this.serialized = serialized;
    this.normalized = normalized;
  }

  public String getSerialized() {
    return serialized;
  }

  public DirectionName getNormalized() {
    return normalized;
  }

}
