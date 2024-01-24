package lib;

public class BlackjackPlayer extends Player {
    public BlackjackPlayer(String name) {
        super(name);
    }

    public int getScore() {
        int numCards = this.hand.size();
        int score = 0;
        for (int i = 0; i < numCards; i++) {
            Card c = hand.get(i);
            int rank = c.getRank();
            if (rank > 10) {
                score += 10;
            }
            if (rank <= 10 && rank != 1) {
                score += rank;
            }

            if (rank == 1) {
                score += 11;
            }
        }
        for (int i = 0; i < this.hand.size(); i++) {
            if (score > 21) {
                if (this.hand.get(i).getRank() == 1) {
                    score -= 10;
                }
            }
        }
        return score;
    }

    public boolean isBust() {
        if (this.getScore() > 21) {
            return true;
        }
        return false;
    }
}