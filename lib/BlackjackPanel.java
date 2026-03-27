package lib;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BlackjackPanel extends JPanel implements KeyListener {

    // ─── CONSTANTS ───────────────────────────────────────────────

    private enum GameState { READY, BETTING, PLAYER_TURN, DEALER_TURN, RESULTS }

    private static final int FPS            = 10;
    private static final int DEALER_DELAY   = 5;   // ticks between dealer draws (~500 ms)
    private static final int CARD_W         = 80;
    private static final int CARD_H         = 116; // ~1:1.45 aspect ratio
    private static final int CARD_GAP       = 92;
    private static final int[] CHIP_VALUES  = { 10, 25, 50, 100, 200 };

    private static final Color GREEN_WIN    = new Color(100, 220, 100);
    private static final Color RED_LOSS     = new Color(220, 80,  80);
    private static final Color GOLD         = new Color(255, 215, 0);
    private static final Color PANEL_BG     = new Color(0, 0, 0, 160);

    // ─── STATE ───────────────────────────────────────────────────

    private GameState       state;
    private BlackjackPlayer player;
    private BlackjackPlayer dealer;
    private Deck            deck;
    private boolean         playingSplitHand;
    private int             pendingBet;
    private int             dealerTick;
    private String          announcement;
    private Color           announcementColor;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────

    public BlackjackPanel(int w, int h) {
        setFocusable(true);
        setPreferredSize(new Dimension(w, h));
        addKeyListener(this);

        deck   = new Deck(6);          // 6-deck shoe — standard casino
        player = new BlackjackPlayer("Player", 1000);
        dealer = new BlackjackPlayer("Dealer");

        state            = GameState.READY;
        announcement     = "";
        announcementColor = Color.WHITE;

        // Use a Swing Timer so ticks fire on the EDT — safe for Swing repaints
        new Timer(1000 / FPS, e -> { tick(); repaint(); }).start();
    }

    // ─── GAME LOOP ───────────────────────────────────────────────

    /** Called by the Swing Timer on the EDT every frame. */
    private void tick() {
        if (state != GameState.DEALER_TURN) return;
        if (dealerTick > 0) { dealerTick--; return; }

        if (dealer.getScore() < 17) {
            dealer.take(deck.deal());
            dealerTick = DEALER_DELAY; // pause before next card
        } else {
            resolveResults();
            state = GameState.RESULTS;
        }
    }

    /** Legacy entry point called by Runner — the Timer starts in the constructor. */
    public void run() {}

    // ─── DEAL ────────────────────────────────────────────────────

    private void startDeal() {
        if (deck.needsReshuffle()) deck.reset();
        player.clearHand();
        dealer.clearHand();
        playingSplitHand = false;
        announcement     = "";

        // Alternate deal: player, dealer, player, dealer
        player.take(deck.deal());
        dealer.take(deck.deal());
        player.take(deck.deal());
        dealer.take(deck.deal());

        // Check for natural blackjack immediately
        boolean playerBJ = player.isBlackjack();
        boolean dealerBJ = dealer.isBlackjack();

        if (playerBJ || dealerBJ) {
            resolveBlackjack(playerBJ, dealerBJ);
            state = GameState.RESULTS;
        } else {
            state = GameState.PLAYER_TURN;
        }
    }

    private void resolveBlackjack(boolean playerBJ, boolean dealerBJ) {
        int bet = player.getBet();
        if (playerBJ && dealerBJ) {
            player.push();
            player.addTies();
            setAnnouncement("Both Blackjack — Push!", Color.YELLOW);
        } else if (playerBJ) {
            int bonus = (int) (bet * 1.5);
            player.winBlackjack();
            player.addWins();
            setAnnouncement("BLACKJACK! You win $" + bonus + " bonus!", GOLD);
        } else {
            player.loseBet();
            player.addLosses();
            setAnnouncement("Dealer Blackjack — You lose $" + bet, RED_LOSS);
        }
    }

    // ─── PLAYER ACTIONS ──────────────────────────────────────────

    private void playerHit() {
        if (playingSplitHand) {
            player.takeSplit(deck.deal());
            if (player.isSplitBust()) goToDealerTurn();
        } else {
            player.take(deck.deal());
            if (player.isBust()) {
                if (player.hasSplitHand()) {
                    playingSplitHand = true;
                    setAnnouncement("Hand 1 busts — now playing Hand 2", Color.ORANGE);
                } else {
                    resolveResults();
                    state = GameState.RESULTS;
                }
            } else if (player.isDoubledDown()) {
                // After doubling down, automatically stand
                if (player.hasSplitHand()) {
                    playingSplitHand = true;
                    setAnnouncement("Now playing Hand 2", Color.WHITE);
                } else {
                    goToDealerTurn();
                }
            }
        }
    }

    private void playerStand() {
        if (playingSplitHand) {
            goToDealerTurn();
        } else if (player.hasSplitHand()) {
            playingSplitHand = true;
            setAnnouncement("Now playing Hand 2", Color.WHITE);
        } else {
            goToDealerTurn();
        }
    }

    private void playerDoubleDown() {
        if (!player.canDoubleDown()) return;
        player.doubleDown();
        playerHit(); // take exactly one card, then auto-stand
    }

    private void playerSplit() {
        if (!player.canSplit()) return;
        Card splitCard = player.split();  // removes second card, deducts matching bet
        player.takeSplit(splitCard);      // seed the split hand with that card
        player.take(deck.deal());         // give main hand a new second card
        player.takeSplit(deck.deal());    // give split hand a new second card
        setAnnouncement("Split! Play Hand 1 first.", Color.WHITE);
    }

    private void goToDealerTurn() {
        announcement = "";
        dealerTick   = DEALER_DELAY;  // brief pause before dealer acts
        state        = GameState.DEALER_TURN;
    }

    // ─── RESULTS ─────────────────────────────────────────────────

    private void resolveResults() {
        int  bet         = player.getBet();
        int  playerScore = player.getScore();
        int  dealerScore = dealer.getScore();
        boolean dealerBust = dealer.isBust();

        // Main hand
        if (player.isBust()) {
            player.loseBet();
            player.addLosses();
            setAnnouncement("Bust! You lose $" + bet, RED_LOSS);
        } else if (dealerBust) {
            player.winBet();
            player.addWins();
            setAnnouncement("Dealer busts — You win $" + bet + "!", GREEN_WIN);
        } else if (playerScore > dealerScore) {
            player.winBet();
            player.addWins();
            setAnnouncement("You win $" + bet + "!", GREEN_WIN);
        } else if (playerScore == dealerScore) {
            player.push();
            player.addTies();
            setAnnouncement("Push — $" + bet + " returned", Color.YELLOW);
        } else {
            player.loseBet();
            player.addLosses();
            setAnnouncement("Dealer wins — You lose $" + bet, RED_LOSS);
        }

        // Split hand (if any)
        if (player.hasSplitHand()) {
            int splitScore = player.getSplitScore();
            int splitBet   = player.getSplitBet();
            String suffix;
            if (player.isSplitBust() || (!dealerBust && splitScore < dealerScore)) {
                player.loseSplitBet();
                suffix = " | Hand 2: -$" + splitBet;
            } else if (!dealerBust && splitScore == dealerScore) {
                player.pushSplit();
                suffix = " | Hand 2: push";
            } else {
                player.winSplitBet();
                suffix = " | Hand 2: +$" + splitBet;
            }
            announcement += suffix;
        }

        if (player.getChips() <= 0 && player.getBet() == 0) {
            setAnnouncement("Out of chips! Press SPACE to restart.", RED_LOSS);
        }
    }

    // ─── PAINTING ────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTable(g2);
        if (state != GameState.READY && state != GameState.BETTING) {
            drawDealerArea(g2);
            drawPlayerArea(g2);
        }
        drawHUD(g2);
        drawCenter(g2);
        drawHint(g2);
    }

    private void drawTable(Graphics2D g) {
        g.setColor(new Color(7, 99, 36));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(9, 120, 44));
        g.fillOval(40, 20, getWidth() - 80, getHeight() - 40);

        g.setColor(new Color(130, 85, 20));
        g.setStroke(new BasicStroke(8f));
        g.drawOval(40, 20, getWidth() - 80, getHeight() - 40);
        g.setStroke(new BasicStroke(1f));

        // Subtle divider line
        g.setColor(new Color(255, 255, 255, 25));
        g.drawLine(120, getHeight() / 2, getWidth() - 120, getHeight() / 2);
    }

    private void drawDealerArea(Graphics2D g) {
        boolean hideHole = state == GameState.PLAYER_TURN;
        List<Card> hand  = dealer.hand;
        int n            = hand.size();
        int startX       = centeredStartX(n);
        int y            = 35;

        for (int i = 0; i < n; i++) {
            int x = startX + i * CARD_GAP;
            Image img = (i == 1 && hideHole) ? Card.getBack() : hand.get(i).getFace();
            g.drawImage(img, x, y, CARD_W, CARD_H, null);
        }

        if (!hand.isEmpty()) {
            g.setFont(font(14, Font.BOLD));
            if (hideHole) {
                drawCentered(g, "Dealer shows: " + hand.get(0), getWidth() / 2, y + CARD_H + 22, Color.LIGHT_GRAY);
            } else {
                String label = "Dealer: " + dealer.getScore();
                if (dealer.isBlackjack()) label += "  ★ BLACKJACK";
                drawCentered(g, label, getWidth() / 2, y + CARD_H + 22, Color.WHITE);
            }
        }
    }

    private void drawPlayerArea(Graphics2D g) {
        int y = getHeight() - CARD_H - 55;
        boolean split = player.hasSplitHand();

        if (!split) {
            drawHand(g, player.hand, getWidth() / 2, y,
                    "Your Hand", player.getScore(), player.isBust(), player.isBlackjack(),
                    true, false);
        } else {
            drawHand(g, player.hand, getWidth() / 4, y,
                    "Hand 1", player.getScore(), player.isBust(), player.isBlackjack(),
                    !playingSplitHand, true);
            drawHand(g, player.getSplitHand(), 3 * getWidth() / 4, y,
                    "Hand 2", player.getSplitScore(), player.isSplitBust(), false,
                    playingSplitHand, true);
        }
    }

    /**
     * Draw one hand of cards centred at {@code cx}.
     *
     * @param active      whether this is the hand currently being played
     * @param applyTint   whether to tint active/inactive hands (only when two hands exist)
     */
    private void drawHand(Graphics2D g, List<Card> cards, int cx, int y,
            String label, int score, boolean bust, boolean blackjack,
            boolean active, boolean applyTint) {
        if (cards.isEmpty()) return;
        int n      = cards.size();
        int totalW = (n - 1) * CARD_GAP + CARD_W;
        int startX = cx - totalW / 2;

        for (int i = 0; i < n; i++) {
            int x = startX + i * CARD_GAP;
            g.drawImage(cards.get(i).getFace(), x, y, CARD_W, CARD_H, null);
            if (applyTint) {
                g.setColor(active
                        ? new Color(255, 255, 100, 50)  // warm highlight for active hand
                        : new Color(0, 0, 0, 110));     // dim inactive hand
                g.fillRect(x, y, CARD_W, CARD_H);
            }
        }

        // Score label above the hand
        String text = label + ": " + score;
        if (blackjack) text += " ★ BJ";
        if (bust)      text += " BUST";
        Color labelColor = bust ? RED_LOSS : (active ? Color.WHITE : Color.GRAY);
        g.setFont(font(14, Font.BOLD));
        drawCentered(g, text, cx, y - 8, labelColor);
    }

    private void drawHUD(Graphics2D g) {
        // ── Chips & Bet (top left) ──
        g.setColor(PANEL_BG);
        g.fillRoundRect(10, 10, 210, 80, 12, 12);

        g.setFont(font(22, Font.BOLD));
        g.setColor(GOLD);
        g.drawString("$" + player.getChips(), 22, 42);

        g.setFont(font(13, Font.PLAIN));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("chips", 22, 58);

        if (player.getBet() > 0) {
            g.setColor(Color.WHITE);
            g.setFont(font(13, Font.BOLD));
            g.drawString("Bet: $" + player.getBet(), 22, 78);
        }

        // ── W / L / T (top right) ──
        g.setColor(PANEL_BG);
        g.fillRoundRect(getWidth() - 175, 10, 165, 80, 12, 12);

        g.setFont(font(15, Font.BOLD));
        g.setColor(GREEN_WIN);  g.drawString("W: " + player.getWins(),   getWidth() - 160, 36);
        g.setColor(RED_LOSS);   g.drawString("L: " + player.getLosses(), getWidth() - 105, 36);
        g.setColor(Color.YELLOW); g.drawString("T: " + player.getTies(), getWidth() -  50, 36);

        g.setFont(font(12, Font.PLAIN));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Cards left: " + deck.getCardsLeft(), getWidth() - 160, 72);
    }

    private void drawCenter(Graphics2D g) {
        int cy = getHeight() / 2;
        switch (state) {
            case READY:
                drawBigText(g, "BLACKJACK", cy - 30, GOLD);
                drawMedText(g, "Press SPACE to play", cy + 18, Color.WHITE);
                break;

            case BETTING:
                drawBigText(g, "Place Your Bet", cy - 50, Color.WHITE);
                if (pendingBet > 0) {
                    drawBigText(g, "$" + pendingBet, cy + 10, GOLD);
                } else {
                    drawMedText(g, "Select chip values below", cy + 10, Color.LIGHT_GRAY);
                }
                break;

            default:
                if (announcement != null && !announcement.isEmpty()) {
                    drawBanner(g, announcement, announcementColor, cy);
                }
                break;
        }
    }

    private void drawBigText(Graphics2D g, String text, int y, Color color) {
        g.setFont(font(42, Font.BOLD));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        // Drop shadow
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(text, x + 3, y + 3);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawMedText(Graphics2D g, String text, int y, Color color) {
        g.setFont(font(18, Font.PLAIN));
        drawCentered(g, text, getWidth() / 2, y, color);
    }

    private void drawBanner(Graphics2D g, String text, Color color, int y) {
        g.setFont(font(24, Font.BOLD));
        FontMetrics fm = g.getFontMetrics();
        int tw      = fm.stringWidth(text);
        int pad     = 18;
        int bx      = (getWidth() - tw) / 2 - pad;
        int bw      = tw + pad * 2;
        int ascent  = fm.getAscent();
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRoundRect(bx, y - ascent - 6, bw, fm.getHeight() + 12, 12, 12);
        g.setColor(color);
        g.drawString(text, bx + pad, y);
    }

    private void drawHint(Graphics2D g) {
        String hint = buildHint();
        if (hint.isEmpty()) return;
        g.setFont(font(12, Font.PLAIN));
        FontMetrics fm = g.getFontMetrics();
        int tw  = fm.stringWidth(hint);
        int x   = (getWidth() - tw) / 2;
        int y   = getHeight() - 10;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(x - 12, y - fm.getAscent() - 3, tw + 24, fm.getHeight() + 6, 8, 8);
        g.setColor(new Color(195, 195, 195));
        g.drawString(hint, x, y);
    }

    // ─── HELPERS ─────────────────────────────────────────────────

    private String buildHint() {
        switch (state) {
            case READY:
                return "SPACE: play";
            case BETTING:
                return "[1]$10  [2]$25  [3]$50  [4]$100  [5]$200  [BACKSPACE] clear  [ENTER] deal";
            case PLAYER_TURN: {
                StringBuilder sb = new StringBuilder("SPACE: Hit   ENTER: Stand");
                if (!playingSplitHand && player.canDoubleDown()) sb.append("   D: Double Down");
                if (!playingSplitHand && player.canSplit())      sb.append("   S: Split");
                return sb.toString();
            }
            case DEALER_TURN:
                return "Dealer is playing...";
            case RESULTS:
                return player.getChips() > 0 ? "SPACE: play again" : "SPACE: restart with $1000";
            default:
                return "";
        }
    }

    private int centeredStartX(int numCards) {
        int totalW = (numCards - 1) * CARD_GAP + CARD_W;
        return (getWidth() - totalW) / 2;
    }

    private void drawCentered(Graphics2D g, String text, int cx, int y, Color color) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }

    private void setAnnouncement(String msg, Color color) {
        announcement      = msg;
        announcementColor = color;
    }

    private Font font(int size, int style) {
        return new Font("Century", style, size);
    }

    // ─── KEY INPUT ───────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (state) {
            case READY:
                if (key == KeyEvent.VK_SPACE) {
                    pendingBet = 0;
                    state = GameState.BETTING;
                }
                break;

            case BETTING:
                handleBettingKey(key);
                break;

            case PLAYER_TURN:
                if (key == KeyEvent.VK_SPACE)                       playerHit();
                if (key == KeyEvent.VK_ENTER)                       playerStand();
                if (key == KeyEvent.VK_D && !playingSplitHand)      playerDoubleDown();
                if (key == KeyEvent.VK_S && !playingSplitHand)      playerSplit();
                break;

            case RESULTS:
                if (key == KeyEvent.VK_SPACE) {
                    if (player.getChips() <= 0) player.resetChips(1000);
                    pendingBet   = 0;
                    announcement = "";
                    state        = GameState.BETTING;
                }
                break;

            default:
                break;
        }
        repaint();
    }

    private void handleBettingKey(int key) {
        int[] keyMap = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
            KeyEvent.VK_4, KeyEvent.VK_5
        };
        for (int i = 0; i < keyMap.length; i++) {
            if (key == keyMap[i]) {
                int add = CHIP_VALUES[i];
                if (pendingBet + add <= player.getChips()) pendingBet += add;
                return;
            }
        }
        if (key == KeyEvent.VK_BACK_SPACE) {
            pendingBet = 0;
        } else if (key == KeyEvent.VK_ENTER && pendingBet > 0) {
            player.placeBet(pendingBet);
            pendingBet = 0;
            startDeal(); // sets state to PLAYER_TURN or RESULTS
        }
    }

    @Override public void keyTyped(KeyEvent e)    {}
    @Override public void keyReleased(KeyEvent e) {}
}
