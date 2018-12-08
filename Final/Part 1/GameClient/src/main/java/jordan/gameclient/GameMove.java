package jordan.gameclient;

public enum GameMove {
    ROCK("ROCK"),
    PAPER("PAPER"),
    SCISSORS("SCISSORS");
    
    private final String display;
    private GameMove(String s) { display = s; }
    @Override public String toString() { return display; }
}
