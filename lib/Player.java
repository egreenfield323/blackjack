package lib;

import java.util.ArrayList;
import java.util.List;

public class Player {
    protected List<Card> hand;
    protected String playerName;
    protected int wins, losses;

    public Player(String name) {
        this.hand = new ArrayList<Card>();
        this.playerName = name;
        this.wins = 0;
        this.losses = 0;
    }

    public void take(Card card) {
        this.hand.add(card);
    }

    public void addWins() {
        this.wins++;
    }

    public void addLosses() {
        this.losses++;
    }

    public int getScore() {
        return 0;
    }

    public String toString() {
        return String.format("Player: %s\n Win/Loss: %d/%d\n Hand: %s", this.playerName,
                this.wins, this.losses,
                this.hand.toString());
    }
}