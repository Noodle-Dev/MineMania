import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Economy.getInstance().reset();
            new LoginWindow();
        });
    }
}


//  ADD YOU OWN API KEY TO "config.propoerties", KEY AVAILABLE AT groq.com