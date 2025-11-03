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