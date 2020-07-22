package ScrabbleBase;

public abstract class TestHarness {
  private final String name;

  public TestHarness(String name) {
    this.name = name;
  }

  public boolean run() {
    System.out.printf("%s...", name);
    this.prepare();
    boolean passed = this.execute();
    System.out.printf("%s.\n", passed ? "Passed" : "Failed");
    return passed;
  }

  protected abstract void prepare();

  protected abstract boolean execute();

}
