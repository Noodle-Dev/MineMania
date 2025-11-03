import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent; // ¡IMPORTACIÓN AÑADIDA AQUÍ!

public class LoginWindow extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    // --- PALETA MODERNA 2024 ---
    private static final Color BG_DARK = new Color(13, 17, 23);
    private static final Color CARD_BG = new Color(22, 27, 34);
    private static final Color ACCENT_PRIMARY = new Color(47, 129, 247);
    private static final Color ACCENT_SECONDARY = new Color(130, 84, 255);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(48, 54, 61);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public LoginWindow() {
        setTitle("Mina Virtual - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        // Panel principal con padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        add(mainPanel, BorderLayout.CENTER);

        // Título
        JLabel title = new JLabel("💎 Mina Virtual", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DARK);

        // Campo Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(UI_FONT);
        emailLabel.setForeground(TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        styleTextField(emailField);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Espaciador
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(emailField);

        // Espaciador
        formPanel.add(Box.createVerticalStrut(20));

        // Campo Contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setFont(UI_FONT);
        passwordLabel.setForeground(TEXT_SECONDARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Espaciador
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);

        // Espaciador
        formPanel.add(Box.createVerticalStrut(30));

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginButton = createModernButton("🔐 Acceder", ACCENT_PRIMARY);
        JButton registerButton = createModernButton("📝 Registrar", ACCENT_SECONDARY);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Agregar componentes al formulario
        formPanel.add(buttonPanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Configurar listeners
        setupEventListeners(loginButton, registerButton);
    }

    private void styleTextField(JComponent field) {
        field.setPreferredSize(new Dimension(400, 45));
        field.setMaximumSize(new Dimension(400, 45));
        field.setBackground(CARD_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setFont(UI_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setCaretColor(TEXT_PRIMARY);
        }
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
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
            showMessage("¡Bienvenido " + email + "!", "Éxito");
            setVisible(false);
            new StoreWindow(email);
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