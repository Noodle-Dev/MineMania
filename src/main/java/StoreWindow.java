import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class StoreWindow extends JFrame {
    private Economy economy;
    private UserState userState;

    private JLabel moneyLabel;
    private JList<String> mineralList;
    private JList<String> inventoryList;
    private javax.swing.Timer refreshTimer;
    private List<String> mineralNames;

    private static final Color BG_LIGHT = new Color(248, 249, 250);
    private static final Color CARD_BG_LIGHT = new Color(255, 255, 255);
    private static final Color ACCENT_PRIMARY = new Color(0, 123, 255);
    private static final Color ACCENT_SECONDARY = new Color(23, 162, 184);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color TEXT_PRIMARY_LIGHT = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY_LIGHT = new Color(108, 117, 125);
    private static final Color BORDER_COLOR_LIGHT = new Color(222, 226, 230);

    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Inter", Font.BOLD, 16);
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font MONEY_FONT = new Font("Inter", Font.BOLD, 18);


    public StoreWindow(UserState userState) {
        this.userState = userState;
        this.economy = Economy.getInstance();
        this.mineralNames = economy.getMineralNames();

        setupUI(userState.getUserEmail());
        startAutoRefresh();

        updateMoneyDisplay();
        updateInventoryDisplay();
    }

    private void setupUI(String userEmail) {
        setTitle("Mina Virtual - " + userEmail);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT);
        setLayout(new BorderLayout(20, 20));

        JPanel topPanel = createTopPanel(userEmail);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane mainSplitPane = createMainSplitPane();
        add(mainSplitPane, BorderLayout.CENTER);

        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setVisible(true);
    }

    private JPanel createTopPanel(String userEmail) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_LIGHT);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_LIGHT);

        JLabel title = new JLabel("💎 Mina Virtual");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY_LIGHT);

        JLabel userLabel = new JLabel("👤 " + userEmail);
        userLabel.setFont(UI_FONT);
        userLabel.setForeground(TEXT_SECONDARY_LIGHT);

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(userLabel, BorderLayout.EAST);

        JPanel moneyPanel = new JPanel();
        moneyPanel.setBackground(CARD_BG_LIGHT);
        moneyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        moneyLabel = new JLabel("$" + String.format("%.2f", userState.getMoney()));
        moneyLabel.setFont(MONEY_FONT);
        moneyLabel.setForeground(SUCCESS_COLOR);
        moneyPanel.add(moneyLabel);

        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(moneyPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JSplitPane createMainSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setDividerSize(10);
        splitPane.setBackground(BG_LIGHT);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        splitPane.setOpaque(false);

        JPanel storePanel = createStorePanel();
        splitPane.setLeftComponent(storePanel);

        JPanel inventoryPanel = createInventoryPanel();
        splitPane.setRightComponent(inventoryPanel);

        return splitPane;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JPanel createStorePanel() {
        JPanel storePanel = createCardPanel();

        JLabel storeHeader = new JLabel("🛒 Tienda de Minerales");
        storeHeader.setFont(SUBTITLE_FONT);
        storeHeader.setForeground(TEXT_PRIMARY_LIGHT);
        storeHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        String[] mineralArray = new String[mineralNames.size()];
        for (int i = 0; i < mineralNames.size(); i++) {
            String mineral = mineralNames.get(i);
            mineralArray[i] = mineral + " - $" + economy.getBuyPrice(mineral);
        }

        mineralList = new JList<>(mineralArray);
        applyModernListStyle(mineralList, ACCENT_PRIMARY);
        JScrollPane scrollPane = new JScrollPane(mineralList);
        styleScrollPane(scrollPane);

        JButton buyButton = createModernButton("🛒 Comprar Mineral", ACCENT_PRIMARY, Color.WHITE);
        buyButton.addActionListener(e -> buyMineral());

        storePanel.add(storeHeader, BorderLayout.NORTH);
        storePanel.add(scrollPane, BorderLayout.CENTER);
        storePanel.add(buyButton, BorderLayout.SOUTH);

        return storePanel;
    }

    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = createCardPanel();

        JLabel inventoryHeader = new JLabel("📦 Tu Inventario");
        inventoryHeader.setFont(SUBTITLE_FONT);
        inventoryHeader.setForeground(TEXT_PRIMARY_LIGHT);
        inventoryHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        inventoryList = new JList<>(new String[]{"Inventario vacío"});
        applyModernListStyle(inventoryList, ACCENT_SECONDARY);
        JScrollPane scrollPane = new JScrollPane(inventoryList);
        styleScrollPane(scrollPane);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(CARD_BG_LIGHT);

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
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
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

    private void applyModernListStyle(JList<String> list, Color accentColor) {
        list.setBackground(CARD_BG_LIGHT);
        list.setForeground(TEXT_PRIMARY_LIGHT);
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
                } else {
                    label.setBackground(CARD_BG_LIGHT);
                    label.setForeground(TEXT_PRIMARY_LIGHT);
                }

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
        vertical.setBorder(null);
    }

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
        double price = economy.getBuyPrice(mineralName);

        if (userState.getMoney() >= price) {
            userState.setMoney(userState.getMoney() - price);
            userState.addToInventory(mineralName, 1);

            UserDataService.saveUserState(userState);

            updateMoneyDisplay();
            updateInventoryDisplay();
            showMessage("¡Compraste " + mineralName + "!", "Éxito");

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
        if (selectedValue == null || selectedValue.equals("Inventario vacío")) {
            showMessage("No tienes minerales para vender!", "Información");
            return;
        }

        String mineralName = selectedValue.split(" - ")[0].trim();

        if (userState.removeFromInventory(mineralName, 1)) {
            double sellPrice = economy.getSellPrice(mineralName);

            userState.setMoney(userState.getMoney() + sellPrice);

            UserDataService.saveUserState(userState);

            updateMoneyDisplay();
            updateInventoryDisplay();
            showMessage("¡Vendiste " + mineralName + " por $" +
                    String.format("%.2f", sellPrice) + "!", "Éxito");
        } else {
            showMessage("Error: No tienes " + mineralName + " para vender.", "Error");
        }
    }

    private void refreshPrices() {
        economy.forcePriceUpdate();

        String[] mineralArray = new String[mineralNames.size()];
        for (int i = 0; i < mineralNames.size(); i++) {
            String mineral = mineralNames.get(i);
            mineralArray[i] = mineral + " - $" + economy.getBuyPrice(mineral);

            userState.getInventory().putIfAbsent(mineral, 0);
        }
        mineralList.setListData(mineralArray);

        updateInventoryDisplay();
    }

    private void updateMoneyDisplay() {
        moneyLabel.setText("$" + String.format("%.2f", userState.getMoney()));
    }

    private void updateInventoryDisplay() {
        java.util.List<String> inventoryItems = new java.util.ArrayList<>();

        for (Map.Entry<String, Integer> entry : userState.getInventory().entrySet()) {
            String mineral = entry.getKey();
            int quantity = entry.getValue();

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