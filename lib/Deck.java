package lib;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    protected List<Card> deck;

    public Deck() {
        this.deck = new ArrayList<Card>();
        for (int ranks = 1; ranks <= 13; ranks++) {
            for (int suits = 0; suits < 4; suits++) {
                deck.add(new Card(ranks, suits));
            }
        }
    }

    public Card deal() {
        return this.deck.remove(0);
    }

    public int getCardsLeft() {
        return this.deck.size();
    }

    public void shuffle() {
        final int ITERATIONS = 1000;
        for (int i = 0; i < ITERATIONS; i++) {
            double indexRange = Math.random() * this.deck.size();
            Card removed = this.deck.remove((int) indexRange);
            this.deck.add(removed);
        }
    }

    public String toString() {
        String str = "";
        for (Card card : deck) {
            str += card + "\n";
        }
        return str;
    }

}