import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public class LoginWindow extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    private static final Color BG_LIGHT = new Color(248, 249, 250);
    private static final Color CARD_BG_LIGHT = new Color(255, 255, 255);
    private static final Color ACCENT_PRIMARY = new Color(0, 123, 255);
    private static final Color ACCENT_SECONDARY = new Color(108, 117, 125);
    private static final Color TEXT_PRIMARY_LIGHT = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY_LIGHT = new Color(108, 117, 125);
    private static final Color BORDER_COLOR_LIGHT = new Color(222, 226, 230);

    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 28);
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 14);

    public LoginWindow() {
        setTitle("Mina Virtual - Login");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout());

        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        add(mainPanel, BorderLayout.CENTER);

        JLabel title = new JLabel("💎 Mina Virtual", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY_LIGHT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_LIGHT);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(UI_FONT);
        emailLabel.setForeground(TEXT_SECONDARY_LIGHT);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        styleTextField(emailField);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(emailField);

        formPanel.add(Box.createVerticalStrut(20));

        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setFont(UI_FONT);
        passwordLabel.setForeground(TEXT_SECONDARY_LIGHT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);

        formPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(BG_LIGHT);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginButton = createModernButton("🔐 Acceder", ACCENT_PRIMARY, Color.WHITE);
        JButton registerButton = createModernButton("📝 Registrar", ACCENT_SECONDARY, Color.WHITE);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        setupEventListeners(loginButton, registerButton);
    }

    private void styleTextField(JComponent field) {
        field.setPreferredSize(new Dimension(400, 45));
        field.setMaximumSize(new Dimension(400, 45));
        field.setBackground(CARD_BG_LIGHT);
        field.setForeground(TEXT_PRIMARY_LIGHT);
        field.setFont(UI_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setCaretColor(TEXT_PRIMARY_LIGHT);
        }
    }

    private JButton createModernButton(String text, Color color, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(textColor);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void setupEventListeners(JButton loginButton, JButton registerButton) {
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Por favor, completa todos los campos.", "Información");
            return;
        }

        if (UserStore.login(email, password)) {
            UserState userState = UserDataService.loadUserState(email);

            showMessage("¡Bienvenido " + email + "!", "Éxito");

            new StoreWindow(userState);

            dispose();
        } else {
            showMessage("Credenciales incorrectas. Verifica tu email y contraseña.", "Error");
        }
    }

    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Por favor, completa todos los campos para registrarte.", "Información");
            return;
        }

        if (UserStore.register(email, password)) {
            showMessage("¡Registro exitoso! Ya puedes iniciar sesión.", "Éxito");
            emailField.setText("");
            passwordField.setText("");
        } else {
            showMessage("El email ya está en uso. Por favor, usa otro email.", "Error");
        }
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}