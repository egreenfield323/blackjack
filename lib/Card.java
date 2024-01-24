package lib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Card {

    public static final String[] RANKS = { null, "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen",
            "King" };

    public static final String[] SUITS = { "Clubs", "Diamonds", "Hearts", "Spades" };

    private final int rank, suit;
    private BufferedImage face;
    private static BufferedImage back;

    static {
        String filename = "images/back02.png";
        try {
            back = ImageIO.read(new File(filename));
        } catch (IOException e) {
            back = null;
            System.err.println(e + " file: " + filename);
        }
    }

    public Card(int rank, int suit) {
        if (rank > 13 || rank < 1) {
            throw new IllegalArgumentException("rank " + rank + " is invalid");
        } else if (suit > 3 || suit < 0) {
            throw new IllegalArgumentException("suit " + suit + " is invalid");
        }
        this.rank = rank;
        this.suit = suit;
        String filename = "images/card";
        int cardNum = rank * 4 + suit;
        cardNum -= 3;
        filename += String.format("%02d.png", cardNum);
        try {
            this.face = ImageIO.read(new File(filename));
        } catch (IOException e) {
            this.face = null;
            System.err.println(e + " file: " + filename);
        }
    }

    public BufferedImage getFace() {
        return this.face;
    }

    public BufferedImage getBack() {
        return back;
    }

    public int getRank() {
        return this.rank;

    }

    public int getSuit() {
        return this.suit;
    }

    public boolean equals(Card that) {
        return this.compareTo(that) == 0;
    }

    public int compareTo(Card that) {
        int thisNum = this.rank * 4 + this.suit;
        int thatNum = that.rank * 4 + that.suit;
        return thisNum - thatNum;
    }

    public String toString() {
        return RANKS[this.rank] + " of " + SUITS[this.suit];
    }
}