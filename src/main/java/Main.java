import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Ejecuta la GUI en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            Economy.getInstance().reset();
            new LoginWindow();
        });
    }
}



/*
* API KEY
*
* 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
*
* */