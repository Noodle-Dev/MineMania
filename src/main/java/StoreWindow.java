import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

// NUEVOS IMPORTS para XChart
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class StoreWindow extends JFrame {
    private Economy economy;
    private UserState userState;

    private JLabel moneyLabel;
    private JList<String> mineralList;
    private JList<String> inventoryList;
    private javax.swing.Timer refreshTimer;
    private List<String> mineralNames;

    // NUEVO: Componentes para el gráfico
    private JPanel chartPanel;
    private JLabel chartTitle;
    private String currentAnalyzedMineral;

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
        setSize(1200, 800); // NUEVO: Ventana más grande para acomodar el gráfico
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
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // NUEVO: Cambiado a división vertical
        mainSplitPane.setDividerLocation(400); // NUEVO: Ajustar la división
        mainSplitPane.setDividerSize(10);
        mainSplitPane.setBackground(BG_LIGHT);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        mainSplitPane.setOpaque(false);

        // Panel superior: Tienda e Inventario
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setDividerLocation(450);
        topSplitPane.setDividerSize(5);

        JPanel storePanel = createStorePanel();
        JPanel inventoryPanel = createInventoryPanel();

        topSplitPane.setLeftComponent(storePanel);
        topSplitPane.setRightComponent(inventoryPanel);

        // Panel inferior: Gráfico (NUEVO)
        JPanel chartContainer = createChartPanel();

        mainSplitPane.setTopComponent(topSplitPane);
        mainSplitPane.setBottomComponent(chartContainer);

        return mainSplitPane;
    }

    private JPanel createChartPanel() {
        JPanel chartContainer = createCardPanel();
        chartContainer.setLayout(new BorderLayout(0, 10));

        // Título del gráfico
        chartTitle = new JLabel("📊 Gráfico de Análisis - Selecciona un mineral para analizar");
        chartTitle.setFont(SUBTITLE_FONT);
        chartTitle.setForeground(TEXT_PRIMARY_LIGHT);
        chartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Panel para el gráfico (inicialmente vacío)
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BG_LIGHT);
        chartPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_LIGHT, 1));
        chartPanel.setPreferredSize(new Dimension(800, 300));

        // Mensaje inicial
        JLabel initialMessage = new JLabel("Haz clic en 'Analizar y Comprar' para ver el gráfico", JLabel.CENTER);
        initialMessage.setFont(UI_FONT);
        initialMessage.setForeground(TEXT_SECONDARY_LIGHT);
        chartPanel.add(initialMessage, BorderLayout.CENTER);

        // Botón para actualizar gráfico
        JButton refreshChartButton = createModernButton("🔄 Actualizar Gráfico", ACCENT_SECONDARY, Color.WHITE);
        refreshChartButton.addActionListener(e -> refreshCurrentChart());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_BG_LIGHT);
        buttonPanel.add(refreshChartButton);

        chartContainer.add(chartTitle, BorderLayout.NORTH);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.add(buttonPanel, BorderLayout.SOUTH);

        return chartContainer;
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

        JButton buyButton = createModernButton("📊 Analizar y Comprar", ACCENT_PRIMARY, Color.WHITE);
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

        try {
            // Obtener historial de precios
            List<PriceHistory.PricePoint> priceHistory = economy.getPriceHistory(mineralName);

            // Mostrar gráfico integrado en lugar de ventana separada
            showIntegratedChart(mineralName, priceHistory, price);

        } catch (Exception e) {
            System.err.println("Error mostrando gráfico: " + e.getMessage());
            // Fallback: compra directa sin gráfico
            int response = JOptionPane.showConfirmDialog(this,
                    "Error al cargar el gráfico. ¿Deseas comprar " + mineralName + " por $" +
                            String.format("%.2f", price) + "?",
                    "Confirmar Compra",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                executePurchase(mineralName, price);
            }
        }
    }

    // NUEVO: Método para mostrar gráfico integrado
    private void showIntegratedChart(String mineralName, List<PriceHistory.PricePoint> priceHistory, double currentPrice) {
        currentAnalyzedMineral = mineralName;

        // Actualizar título del gráfico
        chartTitle.setText("📊 Análisis - " + mineralName + " (Precio: $" + String.format("%.2f", currentPrice) + ")");

        // Crear el gráfico apropiado
        Object chart = createChartForMineral(mineralName, currentPrice, priceHistory);

        // Limpiar el panel del gráfico
        chartPanel.removeAll();

        // Añadir el nuevo gráfico
        if (chart instanceof OHLCChart) {
            chartPanel.add(new XChartPanel<>((OHLCChart) chart), BorderLayout.CENTER);
        } else if (chart instanceof XYChart) {
            chartPanel.add(new XChartPanel<>((XYChart) chart), BorderLayout.CENTER);
        } else {
            JLabel errorLabel = new JLabel("Error al crear el gráfico", JLabel.CENTER);
            errorLabel.setFont(UI_FONT);
            errorLabel.setForeground(Color.RED);
            chartPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Añadir panel de botones de compra
        JPanel purchasePanel = new JPanel(new FlowLayout());
        purchasePanel.setBackground(CARD_BG_LIGHT);

        JButton confirmBuyButton = createModernButton("✅ Confirmar Compra - $" + String.format("%.2f", currentPrice), SUCCESS_COLOR, Color.WHITE);
        confirmBuyButton.addActionListener(e -> executePurchase(mineralName, currentPrice));

        JButton cancelButton = createModernButton("❌ Cancelar", new Color(220, 53, 69), Color.WHITE);
        cancelButton.addActionListener(e -> {
            chartTitle.setText("📊 Gráfico de Análisis - Selecciona un mineral para analizar");
            chartPanel.removeAll();
            JLabel initialMessage = new JLabel("Haz clic en 'Analizar y Comprar' para ver el gráfico", JLabel.CENTER);
            initialMessage.setFont(UI_FONT);
            initialMessage.setForeground(TEXT_SECONDARY_LIGHT);
            chartPanel.add(initialMessage, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            currentAnalyzedMineral = null;
        });

        purchasePanel.add(confirmBuyButton);
        purchasePanel.add(cancelButton);

        chartPanel.add(purchasePanel, BorderLayout.SOUTH);

        // Actualizar la UI
        chartPanel.revalidate();
        chartPanel.repaint();

        // Mover el divisor para mostrar el gráfico
        JSplitPane mainSplitPane = (JSplitPane) getContentPane().getComponent(1);
        mainSplitPane.setDividerLocation(0.5); // Mostrar mitad superior e inferior
    }

    // NUEVO: Método para crear gráficos (similar al de CandlestickChart)
    private Object createChartForMineral(String mineralName, double currentPrice, List<PriceHistory.PricePoint> priceHistory) {
        if (priceHistory.size() >= 3) {
            try {
                List<Double> timeData = new ArrayList<>();
                List<Double> openData = new ArrayList<>();
                List<Double> highData = new ArrayList<>();
                List<Double> lowData = new ArrayList<>();
                List<Double> closeData = new ArrayList<>();

                createCandleData(priceHistory, timeData, openData, highData, lowData, closeData);

                OHLCChart chart = new OHLCChartBuilder()
                        .width(700)
                        .height(250)
                        .title("")
                        .xAxisTitle("Tiempo")
                        .yAxisTitle("Precio ($)")
                        .build();

                chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
                chart.getStyler().setDefaultSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Candle);

                OHLCSeries series = chart.addSeries(mineralName, timeData, openData, highData, lowData, closeData);
                series.setUpColor(Color.GREEN);
                series.setDownColor(Color.RED);

                return chart;
            } catch (Exception e) {
                System.err.println("Error creando gráfico de velas: " + e.getMessage());
            }
        }

        // Gráfico de línea como fallback
        XYChart chart = new XYChartBuilder()
                .width(700)
                .height(250)
                .title("")
                .xAxisTitle("Tiempo")
                .yAxisTitle("Precio ($)")
                .build();

        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        for (int i = 0; i < priceHistory.size(); i++) {
            xData.add((double) i);
            yData.add(priceHistory.get(i).getPrice());
        }

        XYSeries series = chart.addSeries("Tendencia", xData, yData);
        series.setMarker(SeriesMarkers.NONE);
        series.setLineColor(Color.BLUE);

        // Añadir línea del precio actual
        if (!xData.isEmpty()) {
            XYSeries currentPriceSeries = chart.addSeries("Precio Actual",
                    new double[]{xData.get(0), xData.get(xData.size()-1)},
                    new double[]{currentPrice, currentPrice});
            currentPriceSeries.setMarker(SeriesMarkers.NONE);
            currentPriceSeries.setLineColor(Color.RED);
            currentPriceSeries.setLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        }

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);

        return chart;
    }

    // NUEVO: Método para crear datos de velas
    private void createCandleData(List<PriceHistory.PricePoint> priceHistory,
                                  List<Double> timeData, List<Double> openData,
                                  List<Double> highData, List<Double> lowData,
                                  List<Double> closeData) {

        if (priceHistory.size() < 2) {
            double price = priceHistory.get(0).getPrice();
            timeData.add(0.0);
            openData.add(price);
            highData.add(price * 1.1);
            lowData.add(price * 0.9);
            closeData.add(price);
            return;
        }

        int pointsPerCandle = Math.max(1, (int) Math.ceil(priceHistory.size() / 5.0));

        for (int i = 0; i < priceHistory.size(); i += pointsPerCandle) {
            int endIndex = Math.min(i + pointsPerCandle, priceHistory.size());
            List<PriceHistory.PricePoint> candlePoints = priceHistory.subList(i, endIndex);

            if (candlePoints.isEmpty()) continue;

            double open = candlePoints.get(0).getPrice();
            double close = candlePoints.get(candlePoints.size() - 1).getPrice();
            double high = candlePoints.stream().mapToDouble(PriceHistory.PricePoint::getPrice).max().orElse(open);
            double low = candlePoints.stream().mapToDouble(PriceHistory.PricePoint::getPrice).min().orElse(open);

            timeData.add((double) i);
            openData.add(open);
            highData.add(high);
            lowData.add(low);
            closeData.add(close);
        }
    }

    // NUEVO: Método para actualizar el gráfico actual
    private void refreshCurrentChart() {
        if (currentAnalyzedMineral != null) {
            double currentPrice = economy.getBuyPrice(currentAnalyzedMineral);
            List<PriceHistory.PricePoint> priceHistory = economy.getPriceHistory(currentAnalyzedMineral);
            showIntegratedChart(currentAnalyzedMineral, priceHistory, currentPrice);
        } else {
            showMessage("No hay ningún mineral siendo analizado actualmente", "Información");
        }
    }

    private void executePurchase(String mineralName, double price) {
        if (userState.getMoney() >= price) {
            userState.setMoney(userState.getMoney() - price);
            userState.addToInventory(mineralName, 1);

            UserDataService.saveUserState(userState);

            updateMoneyDisplay();
            updateInventoryDisplay();
            showMessage("¡Compraste " + mineralName + "!", "Éxito");

            // Limpiar el gráfico después de comprar
            chartTitle.setText("📊 Gráfico de Análisis - Selecciona un mineral para analizar");
            chartPanel.removeAll();
            JLabel initialMessage = new JLabel("Haz clic en 'Analizar y Comprar' para ver el gráfico", JLabel.CENTER);
            initialMessage.setFont(UI_FONT);
            initialMessage.setForeground(TEXT_SECONDARY_LIGHT);
            chartPanel.add(initialMessage, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            currentAnalyzedMineral = null;

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

        // Actualizar gráfico si hay uno mostrándose
        if (currentAnalyzedMineral != null) {
            refreshCurrentChart();
        }
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