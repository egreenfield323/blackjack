package lib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Card implements Comparable<Card> {

    public static final String[] RANKS = { null, "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen",
            "King" };

    public static final String[] SUITS = { "Clubs", "Diamonds", "Hearts", "Spades" };

    private static final String IMAGE_DIR = "images/";

    private final int rank, suit;
    private final BufferedImage face;
    private static final BufferedImage back;

    static {
        String filename = IMAGE_DIR + "back02.png";
        BufferedImage tmp = null;
        try {
            tmp = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.err.println(e + " file: " + filename);
        }
        back = tmp;
    }

    public Card(int rank, int suit) {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("rank " + rank + " is invalid");
        }
        if (suit < 0 || suit > 3) {
            throw new IllegalArgumentException("suit " + suit + " is invalid");
        }
        this.rank = rank;
        this.suit = suit;
        String filename = IMAGE_DIR + String.format("card%02d.png", rank * 4 + suit - 3);
        BufferedImage tmp = null;
        try {
            tmp = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.err.println(e + " file: " + filename);
        }
        this.face = tmp;
    }

    /** Returns the blackjack point value: Ace=11, face cards=10, others=face value. */
    public int getValue() {
        if (rank == 1) return 11;
        if (rank > 10) return 10;
        return rank;
    }

    public boolean isAce() { return rank == 1; }
    public boolean isFaceCard() { return rank > 10; }

    public BufferedImage getFace() {
        return face;
    }

    public static BufferedImage getBack() {
        return back;
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card that) {
        int diff = this.rank - that.rank;
        return diff != 0 ? diff : this.suit - that.suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        return compareTo((Card) obj) == 0;
    }

    @Override
    public int hashCode() {
        return rank * 4 + suit;
    }

    @Override
    public String toString() {
        return RANKS[rank] + " of " + SUITS[suit];
    }
}
