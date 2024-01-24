package lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.Font;

public class BlackjackPanel extends JPanel implements MouseListener, KeyListener {

    // put class & instance variables to control animation here
    int fps;
    String state;
    String keyInput;
    private List<BlackjackPlayer> players;
    private List<Card> removed;
    private BlackjackPlayer computer = new BlackjackPlayer("computer");
    int going;
    boolean asked;
    String announcement;
    boolean addedStat;
    Deck deck = new Deck();
    int numPlayers = 1;

    public BlackjackPanel(int w, int h) {
        setFocusable(true);
        setPreferredSize(new Dimension(w, h));
        setBackground(new Color(10, 60, 10));
        addedStat = false;
        fps = 10;
        going = 0;
        asked = false;
        players = new ArrayList<BlackjackPlayer>();
        removed = new ArrayList<Card>();
        addMouseListener(this);
        addKeyListener(this);
        for (int i = 0; i < numPlayers; i++) {
            players.add(new BlackjackPlayer("player" + i));
        }
        state = "READY";

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int numPlayers = players.size();
        int spaceLeft = 50;
        int size;
        g.setColor(Color.WHITE);
        for (BlackjackPlayer player : players) {
            String stats = "current points: " + player.getScore() + "\nW/L: " + player.wins +
                    "/" + player.losses;
            g.setFont(new Font("Century", Font.BOLD, 15));
            g.drawString(stats, spaceLeft, 320);
            spaceLeft += 220;

        }
        spaceLeft = 0;
        g.setFont(new Font("Century", Font.BOLD, 30));
        g.drawString(announcement, getWidth() / 2 - 150, getHeight() - 275);
        if (numPlayers > 1) {
            size = 50;
        } else {
            size = 100;
        }
        for (int i = 0; i < players.size(); i++) {
            spaceLeft += 50;
            for (Card c : players.get(i).hand) {
                g.drawImage(c.getFace(), spaceLeft, getHeight() - 250, size, size * 2, null);
                spaceLeft += 80;
            }
        }
        if (state == "PlayersTurn") {
            g.drawImage(computer.hand.get(0).getFace(), 50, getHeight() - 520, size, size * 2,
                    null);
            g.drawImage(computer.hand.get(1).getBack(), 130, getHeight() - 520, size, size *
                    2, null);
        }
        if (state == "ComputerTurn") {
            String stats = "current points: " + computer.getScore() + "\nW/L: " + computer.wins + "/" + computer.losses;
            g.setFont(new Font("Century", Font.BOLD, 15));
            g.drawString(stats, 50, 50);
            int center = 50;
            for (Card c : computer.hand) {
                g.drawImage(c.getFace(), center, getHeight() - 520, size, size * 2, null);
                center += 80;
            }
        }
    }

    public void run() {
        while (true) {
            update();
            repaint();
            delay(1000 / fps);
        }
    }

    public void update() {
        if (state == "READY") {
            announcement = "hit [SPACE BAR] to draw cards";
            if (keyInput == "SPACE") {
                deck.shuffle();
                state = "DEAL";
                keyInput = "";
                announcement = "";
            }
        } else if (state == "DEAL") {
            for (int i = 0; i < players.size(); i++) {
                players.get(i).hand.add(deck.deal());
                players.get(i).hand.add(deck.deal());
            }
            computer.hand.add(deck.deal());

            computer.hand.add(deck.deal());
            state = "PlayersTurn";
            announcement = "Player " + going + ", hit (space) or stand? (enter)";
        } else if (state == "PlayersTurn") {
            if (!asked) {
                announcement = "Player " + going + ", hit (space) or stand? (enter)";
                asked = true;
            }
            if (keyInput == "SPACE") {
                announcement = "player" + going + " hit";
                keyInput = "";
                players.get(going).hand.add(deck.deal());
                announcement = "keep going? hit [space] or continue [enter]";
                if (keyInput == "ENTER") {
                    state = "ComputerTurn";
                    keyInput = "";
                } else if (keyInput == "SPACE") {
                    state = "PlayersTurn";
                    keyInput = "";
                }
                if (players.get(going).getScore() > 21) {
                    state = "RESULTS";
                }
            } else if (keyInput == "ENTER") {
                announcement = "player" + going + " is standing";
                keyInput = "";
                state = "ComputerTurn";
            }
        } else if (state == "ComputerTurn") {
            announcement = "";
            if (players.size() - 1 > going) {
                going++;
                state = "PlayersTurn";
                asked = false;
            } else if (computer.getScore() < 17) {
                computer.hand.add(deck.deal());
            }
            announcement = "Press [R] to get results";
            if (keyInput == "r") {
                announcement = "";
                state = "RESULTS";
                keyInput = "";
            }

        } else if (state == "RESULTS") {
            if (computer.getScore() > 21) {
                for (int i = 0; i < players.size(); i++) {
                    if (!addedStat) {
                        players.get(i).addWins();
                        addedStat = true;
                    }
                    announcement = "player " + i + " has won. [SPACE] to continue";
                }
            } else {
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).getScore() == computer.getScore()) {
                        announcement = "tie! press [space] to continue";

                    } else if (players.get(i).getScore() > 21) {
                        if (!addedStat) {
                            players.get(i).addLosses();
                            addedStat = true;
                        }
                        announcement = "player " + i + " has lost. [SPACE] to continue";
                    } else if (players.get(i).getScore() > computer.getScore()) {
                        if (!addedStat) {
                            players.get(i).addWins();
                            addedStat = true;
                        }
                        announcement = "player " + i + " has won. [SPACE] to continue";
                    } else {
                        if (!addedStat) {
                            players.get(i).addLosses();
                            addedStat = true;
                        }
                        announcement = "player " + i + " has lost. [SPACE] to continue";
                    }
                }
            }
            if (keyInput == "SPACE") {
                addedStat = false;
                announcement = "";
                state = "RESET";
                keyInput = "";
            }
        } else if (state == "RESET") {
            announcement = "press [SPACE] to play another hand";
            if (keyInput == "SPACE") {
                keyInput = "";
                for (int i = 0; i < players.size(); i++) {
                    for (Card c : players.get(i).hand) {
                        removed.add(c);
                    }
                }
                for (BlackjackPlayer player : players) {
                    player.hand.clear();
                }

                for (Card c : computer.hand) {
                    removed.add(c);
                }
                computer.hand.clear();
                for (Card c : removed) {
                    deck.deck.add(c);
                }
                removed.clear();
                announcement = "";
                state = "READY";
            }

        } else {
            throw new RuntimeException("Unexpected state: " + state);
        }
    }

    public void delay(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            keyInput = "SPACE";
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            keyInput = "ENTER";
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            keyInput = "r";
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}