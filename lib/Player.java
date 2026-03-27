package lib;

import java.util.ArrayList;
import java.util.List;

public class Player {
    protected List<Card> hand;
    protected String playerName;
    protected int wins, losses, ties;

    public Player(String name) {
        this.hand = new ArrayList<>();
        this.playerName = name;
    }

    public void take(Card card) {
        hand.add(card);
    }

    public void clearHand() {
        hand.clear();
    }

    public int getHandSize() {
        return hand.size();
    }

    public String getName() {
        return playerName;
    }

    public void addWins()   { wins++; }
    public void addLosses() { losses++; }
    public void addTies()   { ties++; }

    public int getWins()   { return wins; }
    public int getLosses() { return losses; }
    public int getTies()   { return ties; }

    public int getScore() { return 0; }

    @Override
    public String toString() {
        return String.format("%s | W/L/T: %d/%d/%d | Hand: %s",
                playerName, wins, losses, ties, hand);
    }
}
