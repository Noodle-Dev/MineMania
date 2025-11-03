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

    // --- PALETA MODERNA 2024 ---
    private static final Color BG_DARK = new Color(13, 17, 23);
    private static final Color CARD_BG = new Color(22, 27, 34);
    private static final Color ACCENT_PRIMARY = new Color(47, 129, 247);
    private static final Color ACCENT_SECONDARY = new Color(130, 84, 255);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(48, 54, 61);

    // --- FUENTES MODERNAS ---
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font MONEY_FONT = new Font("Segoe UI", Font.BOLD, 18);

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
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(25, 25));

        // Panel Superior
        JPanel topPanel = createTopPanel(userEmail);
        add(topPanel, BorderLayout.NORTH);

        // Panel Principal
        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createTopPanel(String userEmail) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_DARK);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // Título y usuario
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_DARK);

        JLabel title = new JLabel("💎 Mina Virtual");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);

        JLabel userLabel = new JLabel("👤 " + userEmail);
        userLabel.setFont(UI_FONT);
        userLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(userLabel, BorderLayout.EAST);

        // Panel de dinero
        JPanel moneyPanel = new JPanel();
        moneyPanel.setBackground(CARD_BG);
        moneyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        moneyLabel = new JLabel("$" + String.format("%.2f", economy.getMoney()));
        moneyLabel.setFont(MONEY_FONT);
        moneyLabel.setForeground(SUCCESS_COLOR);
        moneyPanel.add(moneyLabel);

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(moneyPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JSplitPane createMainSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setDividerSize(2);
        splitPane.setBackground(BG_DARK);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));

        // Panel Tienda
        JPanel storePanel = createStorePanel();
        splitPane.setLeftComponent(storePanel);

        // Panel Inventario
        JPanel inventoryPanel = createInventoryPanel();
        splitPane.setRightComponent(inventoryPanel);

        return splitPane;
    }

    private JPanel createStorePanel() {
        JPanel storePanel = new JPanel(new BorderLayout(0, 15));
        storePanel.setBackground(BG_DARK);

        // Header
        JLabel storeHeader = new JLabel("🛒 Tienda de Minerales");
        storeHeader.setFont(SUBTITLE_FONT);
        storeHeader.setForeground(TEXT_PRIMARY);
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
        JButton buyButton = createModernButton("🛒 Comprar Mineral", ACCENT_PRIMARY);
        buyButton.addActionListener(e -> buyMineral());

        storePanel.add(storeHeader, BorderLayout.NORTH);
        storePanel.add(scrollPane, BorderLayout.CENTER);
        storePanel.add(buyButton, BorderLayout.SOUTH);

        return storePanel;
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout(0, 15));
        inventoryPanel.setBackground(BG_DARK);

        // Header
        JLabel inventoryHeader = new JLabel("📦 Tu Inventario");
        inventoryHeader.setFont(SUBTITLE_FONT);
        inventoryHeader.setForeground(TEXT_PRIMARY);
        inventoryHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Lista de inventario
        inventoryList = new JList<>(new String[]{"Inventario vacío"});
        applyModernListStyle(inventoryList, ACCENT_SECONDARY);
        JScrollPane scrollPane = new JScrollPane(inventoryList);
        styleScrollPane(scrollPane);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(BG_DARK);

        JButton sellButton = createModernButton("💰 Vender", ACCENT_SECONDARY);
        JButton refreshButton = createModernButton("🔄 Actualizar", SUCCESS_COLOR);

        sellButton.addActionListener(e -> sellMineral());
        refreshButton.addActionListener(e -> refreshPrices());

        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);

        inventoryPanel.add(inventoryHeader, BorderLayout.NORTH);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        return inventoryPanel;
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT.deriveFont(Font.BOLD, 14f));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
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

    private void applyModernListStyle(JList<String> list, Color accentColor) {
        list.setBackground(CARD_BG);
        list.setForeground(TEXT_PRIMARY);
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
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

                if (isSelected) {
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                            BorderFactory.createEmptyBorder(8, 8, 8, 12)
                    ));
                }
                return label;
            }
        });
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setBackground(CARD_BG);
        vertical.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
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
        if (economy.buyPlant(mineralName)) {
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
            economy.sellPlant(mineralName);
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