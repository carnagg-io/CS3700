package gameclient;

public enum GameEvent {
    CONNECTED("CONNECTED"),
    READY("READY"),
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    WINNER("WINNER");
    
    private final String display;
    private GameEvent(String s) { display = s; }
    @Override public String toString() { return display; }
}
