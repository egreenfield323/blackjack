package lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final int numDecks;
    protected List<Card> deck;

    /** Single-deck constructor. */
    public Deck() {
        this(1);
    }

    /** Multi-deck constructor. Standard casino blackjack uses 6 decks. */
    public Deck(int numDecks) {
        if (numDecks < 1) throw new IllegalArgumentException("Must use at least 1 deck");
        this.numDecks = numDecks;
        this.deck = new ArrayList<>(52 * numDecks); // pre-sized
        reset();
    }

    /** Rebuild all decks and shuffle. */
    public void reset() {
        deck.clear();
        for (int d = 0; d < numDecks; d++) {
            for (int rank = 1; rank <= 13; rank++) {
                for (int suit = 0; suit < 4; suit++) {
                    deck.add(new Card(rank, suit));
                }
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(deck);
    }

    /** Deal from the top of the deck. Resets automatically if empty. */
    public Card deal() {
        if (deck.isEmpty()) reset();
        return deck.remove(deck.size() - 1); // O(1) removal from end
    }

    public int getCardsLeft() { return deck.size(); }
    public boolean isEmpty()  { return deck.isEmpty(); }

    /**
     * Returns true when fewer than 25% of cards remain — a good point to
     * reset between hands (mimics the casino's cut card).
     */
    public boolean needsReshuffle() {
        return deck.size() < (52 * numDecks) / 4;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : deck) sb.append(card).append('\n');
        return sb.toString();
    }
}
