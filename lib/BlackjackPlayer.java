package lib;

import java.util.ArrayList;
import java.util.List;

public class BlackjackPlayer extends Player {

    private int chips;
    private int bet;
    private boolean doubledDown;
    private List<Card> splitHand;
    private int splitBet;

    public BlackjackPlayer(String name) {
        this(name, 0);
    }

    public BlackjackPlayer(String name, int startingChips) {
        super(name);
        this.chips = startingChips;
    }

    // ─── SCORE ───────────────────────────────────────────────────

    @Override
    public int getScore() {
        return calcScore(hand);
    }

    public int getSplitScore() {
        return splitHand == null ? 0 : calcScore(splitHand);
    }

    private int calcScore(List<Card> cards) {
        int score = 0;
        int aces = 0;
        for (Card c : cards) {
            score += c.getValue(); // Ace counts as 11 initially
            if (c.isAce()) aces++;
        }
        // Reduce aces from 11 to 1 as needed to avoid busting
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }
        return score;
    }

    // ─── STATE CHECKS ────────────────────────────────────────────

    public boolean isBust() { return getScore() > 21; }
    public boolean isSplitBust() { return getSplitScore() > 21; }

    /** Natural blackjack: exactly 2 cards totalling 21. */
    public boolean isBlackjack() {
        return hand.size() == 2 && getScore() == 21;
    }

    /** True if the two starting cards share a rank and the player can afford the second bet. */
    public boolean canSplit() {
        return hand.size() == 2
                && hand.get(0).getRank() == hand.get(1).getRank()
                && chips >= bet
                && splitHand == null;
    }

    /** True if the player can afford to double the current bet (main hand only). */
    public boolean canDoubleDown() {
        return hand.size() == 2 && chips >= bet && !doubledDown;
    }

    public boolean isDoubledDown() { return doubledDown; }
    public boolean hasSplitHand()  { return splitHand != null; }

    // ─── BETTING ─────────────────────────────────────────────────

    public void placeBet(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Bet must be positive");
        if (amount > chips) throw new IllegalArgumentException("Insufficient chips");
        bet = amount;
        chips -= amount;
    }

    public void doubleDown() {
        if (!canDoubleDown()) throw new IllegalStateException("Cannot double down");
        chips -= bet;
        bet *= 2;
        doubledDown = true;
    }

    /** Pays 1:1 on the bet. */
    public void winBet() {
        chips += bet * 2;
        bet = 0;
        doubledDown = false;
    }

    /** Pays 3:2 for a natural blackjack. */
    public void winBlackjack() {
        chips += bet + (int) (bet * 1.5);
        bet = 0;
        doubledDown = false;
    }

    /** Bet returned — tie with dealer. */
    public void push() {
        chips += bet;
        bet = 0;
        doubledDown = false;
    }

    /** Bet is lost. */
    public void loseBet() {
        bet = 0;
        doubledDown = false;
    }

    // ─── SPLIT ───────────────────────────────────────────────────

    /**
     * Splits the hand. Removes and returns the second card so the caller can
     * seed the split hand. Deducts the matching bet from chips.
     */
    public Card split() {
        if (!canSplit()) throw new IllegalStateException("Cannot split");
        chips -= bet;
        splitBet = bet;
        splitHand = new ArrayList<>();
        return hand.remove(1);
    }

    public void takeSplit(Card card) {
        if (splitHand != null) splitHand.add(card);
    }

    public List<Card> getSplitHand() {
        return splitHand != null ? splitHand : new ArrayList<>();
    }

    public void winSplitBet()  { chips += splitBet * 2; splitBet = 0; }
    public void pushSplit()    { chips += splitBet;     splitBet = 0; }
    public void loseSplitBet() { splitBet = 0; }

    // ─── ACCESSORS ───────────────────────────────────────────────

    public int getChips()    { return chips; }
    public int getBet()      { return bet; }
    public int getSplitBet() { return splitBet; }

    public void resetChips(int amount) {
        chips = amount;
    }

    // ─── HAND MANAGEMENT ─────────────────────────────────────────

    @Override
    public void clearHand() {
        super.clearHand();
        splitHand = null;
        splitBet = 0;
        // bet and doubledDown are NOT cleared here — they are set by placeBet()
        // before clearHand() is called and resolved by winBet/push/loseBet after the hand.
    }
}
