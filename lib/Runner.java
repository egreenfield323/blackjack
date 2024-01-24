package lib;

import javax.swing.JFrame;

public class Runner {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Blackjack Game");
        BlackjackPanel blackjackPanel = new BlackjackPanel(1100, 600);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(blackjackPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Start the game loop
        blackjackPanel.run();
    }
}
