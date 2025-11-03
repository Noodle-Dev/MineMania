import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class StoreWindow extends JFrame {
    private Economy economy;
    private JLabel moneyLabel;
    private HashMap<String, Integer> inventory;
    private JList<String> mineralList;
    private JList<String> inventoryList;
    private javax.swing.Timer refreshTimer;
    private List<String> mineralNames;

    // --- NUEVA PALETA: MODO CLARO (CLEAN & PROFESSIONAL) ---
    private static final Color BG_LIGHT = new Color(248, 249, 250); // Gris muy claro
    private static final Color CARD_BG_LIGHT = new Color(255, 255, 255); // Blanco
    private static final Color ACCENT_PRIMARY = new Color(0, 123, 255); // Azul primario
    private static final Color ACCENT_SECONDARY = new Color(23, 162, 184); // Teal/Cyan
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69); // Verde
    private static final Color TEXT_PRIMARY_LIGHT = new Color(33, 37, 41); // Casi negro
    private static final Color TEXT_SECONDARY_LIGHT = new Color(108, 117, 125); // Gris oscuro
    private static final Color BORDER_COLOR_LIGHT = new Color(222, 226, 230); // Gris claro

    // --- FUENTES MODERNAS ---
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Inter", Font.BOLD, 16);
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font MONEY_FONT = new Font("Inter", Font.BOLD, 18);


    public StoreWindow(String userEmail) {
        this.economy = Economy.getInstance();
        this.inventory = new HashMap<>();
        this.mineralNames = economy.getMineralNames();

        for (String mineral : mineralNames) {
            inventory.put(mineral, 0);
        }

        setupUI(userEmail);
        startAutoRefresh();
    }

    private void setupUI(String userEmail) {
        setTitle("Mina Virtual - " + userEmail);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT); // Fondo claro
        setLayout(new BorderLayout(20, 20)); // Un poco más de espacio

        // Panel Superior
        JPanel topPanel = createTopPanel(userEmail);
        add(topPanel, BorderLayout.NORTH);

        // Panel Principal
        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);

        // Padding general
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setVisible(true);
    }

    private JPanel createTopPanel(String userEmail) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_LIGHT); // Fondo claro
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Título y usuario
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT); // Fondo claro

        JLabel title = new JLabel("💎 Mina Virtual");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY_LIGHT); // Texto oscuro

        JLabel userLabel = new JLabel("👤 " + userEmail);
        userLabel.setFont(UI_FONT);
        userLabel.setForeground(TEXT_SECONDARY_LIGHT); // Texto secundario

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(userLabel, BorderLayout.EAST);

        // Panel de dinero
        JPanel moneyPanel = new JPanel();
        moneyPanel.setBackground(CARD_BG_LIGHT); // Fondo blanco
        moneyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1), // Borde claro
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        moneyLabel = new JLabel("$" + String.format("%.2f", economy.getMoney()));
        moneyLabel.setFont(MONEY_FONT);
        moneyLabel.setForeground(SUCCESS_COLOR); // Verde
        moneyPanel.add(moneyLabel);

        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(moneyPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JSplitPane createMainSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setDividerSize(10); // Divisor más notable
        splitPane.setBackground(BG_LIGHT);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20)); // Padding
        splitPane.setOpaque(false);

        // Panel Tienda
        JPanel storePanel = createStorePanel();
        splitPane.setLeftComponent(storePanel);

        // Panel Inventario
        JPanel inventoryPanel = createInventoryPanel();
        splitPane.setRightComponent(inventoryPanel);

        return splitPane;
    }

    // Método helper para crear paneles de "tarjeta"
    private JPanel createCardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20) // Padding interno
        ));
        return card;
    }

    private JPanel createStorePanel() {
        JPanel storePanel = createCardPanel(); // Usa el panel de tarjeta

        // Header
        JLabel storeHeader = new JLabel("🛒 Tienda de Minerales");
        storeHeader.setFont(SUBTITLE_FONT);
        storeHeader.setForeground(TEXT_PRIMARY_LIGHT);
        storeHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Lista de minerales
        String[] mineralArray = new String[mineralNames.size()];
        for (int i = 0; i < mineralNames.size(); i++) {
            String mineral = mineralNames.get(i);
            mineralArray[i] = mineral + " - $" + economy.getBuyPrice(mineral);
        }

        mineralList = new JList<>(mineralArray);
        applyModernListStyle(mineralList, ACCENT_PRIMARY);
        JScrollPane scrollPane = new JScrollPane(mineralList);
        styleScrollPane(scrollPane);

        // Botón Comprar
        JButton buyButton = createModernButton("🛒 Comprar Mineral", ACCENT_PRIMARY, Color.WHITE);
        buyButton.addActionListener(e -> buyMineral());

        storePanel.add(storeHeader, BorderLayout.NORTH);
        storePanel.add(scrollPane, BorderLayout.CENTER);
        storePanel.add(buyButton, BorderLayout.SOUTH);

        return storePanel;
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = createCardPanel(); // Usa el panel de tarjeta

        // Header
        JLabel inventoryHeader = new JLabel("📦 Tu Inventario");
        inventoryHeader.setFont(SUBTITLE_FONT);
        inventoryHeader.setForeground(TEXT_PRIMARY_LIGHT);
        inventoryHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Lista de inventario
        inventoryList = new JList<>(new String[]{"Inventario vacío"});
        applyModernListStyle(inventoryList, ACCENT_SECONDARY);
        JScrollPane scrollPane = new JScrollPane(inventoryList);
        styleScrollPane(scrollPane);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(CARD_BG_LIGHT); // Fondo blanco

        JButton sellButton = createModernButton("💰 Vender", ACCENT_SECONDARY, Color.WHITE);
        JButton refreshButton = createModernButton("🔄 Actualizar", SUCCESS_COLOR, Color.WHITE);

        sellButton.addActionListener(e -> sellMineral());
        refreshButton.addActionListener(e -> refreshPrices());

        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);

        inventoryPanel.add(inventoryHeader, BorderLayout.NORTH);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return inventoryPanel;
    }

    private JButton createModernButton(String text, Color color, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT.deriveFont(Font.BOLD, 14f));
        button.setForeground(textColor);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20)); // Borde simple
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

    private void applyModernListStyle(JList<String> list, Color accentColor) {
        list.setBackground(CARD_BG_LIGHT); // Fondo blanco
        list.setForeground(TEXT_PRIMARY_LIGHT); // Texto oscuro
        list.setFont(UI_FONT);
        list.setSelectionBackground(accentColor);
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        list.setFixedCellHeight(40);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Resetea el borde
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

                if (isSelected) {
                    // Mantiene el fondo y texto de selección, pero añade el borde
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                            BorderFactory.createEmptyBorder(8, 8, 8, 12)
                    ));
                } else {
                    // Asegura que los no seleccionados tengan el fondo correcto
                    label.setBackground(CARD_BG_LIGHT);
                    label.setForeground(TEXT_PRIMARY_LIGHT);
                }

                // Hace que el renderer no sea opaco para que se vea el fondo de la lista
                // Pero en este caso, queremos que sea opaco para mostrar el fondo blanco
                label.setOpaque(true);

                return label;
            }
        });
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG_LIGHT);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setBackground(CARD_BG_LIGHT);
        vertical.setBorder(null); // Más limpio

        // --- Scrollbar más moderna (opcional pero recomendado) ---
        // vertical.setUI(new BasicScrollBarUI() {
        //     @Override
        //     protected void configureScrollBarColors() {
        //         this.thumbColor = ACCENT_SECONDARY;
        //         this.trackColor = BG_LIGHT;
        //     }
        //     @Override
        //     protected JButton createDecreaseButton(int orientation) {
        //         return createZeroButton();
        //     }
        //     @Override
        //     protected JButton createIncreaseButton(int orientation) {
        //         return createZeroButton();
        //     }
        //     private JButton createZeroButton() {
        //         JButton jbutton = new JButton();
        //         jbutton.setPreferredSize(new Dimension(0, 0));
        //         jbutton.setMinimumSize(new Dimension(0, 0));
        //         jbutton.setMaximumSize(new Dimension(0, 0));
        //         return jbutton;
        //     }
        // });
    }

    // --- MÉTODOS DE LÓGICA (sin cambios) ---

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(5000, e -> refreshPrices());
        refreshTimer.start();
    }

    private void buyMineral() {
        int selectedIndex = mineralList.getSelectedIndex();
        if (selectedIndex == -1) {
            showMessage("Selecciona un mineral primero!", "Información");
            return;
        }
        String mineralName = mineralNames.get(selectedIndex);

        if (economy.buyMineral(mineralName)) {
            inventory.put(mineralName, inventory.getOrDefault(mineralName, 0) + 1);
            updateMoneyDisplay();
            updateInventoryDisplay();
            showMessage("¡Compraste " + mineralName + "!", "Éxito");
            refreshPrices();
        } else {
            showMessage("¡No tienes suficiente dinero!", "Error");
        }
    }

    private void sellMineral() {
        int selectedIndex = inventoryList.getSelectedIndex();
        if (selectedIndex == -1) {
            showMessage("Selecciona un mineral del inventario para vender!", "Información");
            return;
        }
        String selectedValue = inventoryList.getSelectedValue();
        if (selectedValue.equals("Inventario vacío")) {
            showMessage("No tienes minerales para vender!", "Información");
            return;
        }
        String mineralName = selectedValue.split(" - ")[0].trim();
        if (inventory.getOrDefault(mineralName, 0) > 0) {
            double sellPrice = economy.getSellPrice(mineralName);
            economy.sellMineral(mineralName);
            inventory.put(mineralName, inventory.get(mineralName) - 1);
            updateMoneyDisplay();
            updateInventoryDisplay();
            showMessage("¡Vendiste " + mineralName + " por $" +
                    String.format("%.2f", sellPrice) + "!", "Éxito");
            refreshPrices();
        }
    }

    private void refreshPrices() {
        economy.forcePriceUpdate();
        this.mineralNames = economy.getMineralNames();
        String[] mineralArray = new String[mineralNames.size()];
        for (int i = 0; i < mineralNames.size(); i++) {
            String mineral = mineralNames.get(i);
            mineralArray[i] = mineral + " - $" + economy.getBuyPrice(mineral);
            inventory.putIfAbsent(mineral, 0);
        }
        mineralList.setListData(mineralArray);
        updateMoneyDisplay();
        updateInventoryDisplay();
    }

    private void updateMoneyDisplay() {
        moneyLabel.setText("$" + String.format("%.2f", economy.getMoney()));
    }

    private void updateInventoryDisplay() {
        java.util.List<String> inventoryItems = new java.util.ArrayList<>();
        for (String mineral : mineralNames) {
            int quantity = inventory.getOrDefault(mineral, 0);
            if (quantity > 0) {
                inventoryItems.add(mineral + " - " + quantity + " (Venta: $" +
                        String.format("%.2f", economy.getSellPrice(mineral)) + ")");
            }
        }
        if (inventoryItems.isEmpty()) {
            inventoryItems.add("Inventario vacío");
        }
        inventoryList.setListData(inventoryItems.toArray(new String[0]));
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}