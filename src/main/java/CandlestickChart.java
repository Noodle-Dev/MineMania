import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CandlestickChart {

    private static JDialog currentDialog;
    private static javax.swing.Timer updateTimer;
    private static String currentMineralName;
    private static Runnable currentOnBuyCallback;
    private static JPanel currentChartPanel;

    public static void showChart(String mineralName, List<PriceHistory.PricePoint> priceHistory,
                                 double currentPrice, Runnable onBuyCallback) {
        currentMineralName = mineralName;
        currentOnBuyCallback = onBuyCallback;

        if (priceHistory.isEmpty() || priceHistory.size() < 2) {
            showSimpleLineChart(mineralName, currentPrice, priceHistory, onBuyCallback);
            return;
        }

        List<Double> timeData = new ArrayList<>();
        List<Double> openData = new ArrayList<>();
        List<Double> highData = new ArrayList<>();
        List<Double> lowData = new ArrayList<>();
        List<Double> closeData = new ArrayList<>();

        createCandleData(priceHistory, timeData, openData, highData, lowData, closeData);

        if (openData.isEmpty() || timeData.isEmpty()) {
            showSimpleLineChart(mineralName, currentPrice, priceHistory, onBuyCallback);
            return;
        }

        try {
            OHLCChart chart = new OHLCChartBuilder()
                    .width(800)
                    .height(500)
                    .title("Gráfico de Velas - " + mineralName + " (Precio Actual: $" + String.format("%.2f", currentPrice) + ")")
                    .xAxisTitle("Tiempo")
                    .yAxisTitle("Precio ($)")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
            chart.getStyler().setDefaultSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Candle);

            OHLCSeries series = chart.addSeries(mineralName, timeData, openData, highData, lowData, closeData);
            series.setUpColor(Color.GREEN);
            series.setDownColor(Color.RED);

            showChartDialog(mineralName, currentPrice, chart, onBuyCallback, priceHistory);

        } catch (Exception e) {
            System.err.println("Error creando gráfico de velas: " + e.getMessage());
            showSimpleLineChart(mineralName, currentPrice, priceHistory, onBuyCallback);
        }
    }

    private static void showSimpleLineChart(String mineralName, double currentPrice,
                                            List<PriceHistory.PricePoint> priceHistory, Runnable onBuyCallback) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(500)
                .title("Tendencia de Precio - " + mineralName + " (Precio Actual: $" + String.format("%.2f", currentPrice) + ")")
                .xAxisTitle("Tiempo")
                .yAxisTitle("Precio ($)")
                .build();

        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        if (priceHistory.isEmpty()) {
            createSimulatedData(currentPrice, xData, yData);
        } else {
            for (int i = 0; i < priceHistory.size(); i++) {
                xData.add((double) i);
                yData.add(priceHistory.get(i).getPrice());
            }
        }

        XYSeries series = chart.addSeries("Tendencia", xData, yData);
        series.setMarker(SeriesMarkers.NONE);
        series.setLineColor(Color.BLUE);

        if (!xData.isEmpty()) {
            XYSeries currentPriceSeries = chart.addSeries("Precio Actual",
                    new double[]{xData.get(0), xData.get(xData.size()-1)},
                    new double[]{currentPrice, currentPrice});
            currentPriceSeries.setMarker(SeriesMarkers.NONE);
            currentPriceSeries.setLineColor(Color.RED);
            currentPriceSeries.setLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        }

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);

        showChartDialog(mineralName, currentPrice, chart, onBuyCallback, priceHistory);
    }

    private static void createSimulatedData(double currentPrice, List<Double> xData, List<Double> yData) {
        Random rand = new Random();
        double basePrice = currentPrice * 0.8;
        for (int i = 0; i < 10; i++) {
            xData.add((double) i);
            double variation = (rand.nextDouble() * 0.4) - 0.2;
            double simulatedPrice = basePrice * (1 + variation);
            simulatedPrice = Math.max(currentPrice * 0.5, Math.min(currentPrice * 1.5, simulatedPrice));
            yData.add(simulatedPrice);
            basePrice = simulatedPrice;
        }
        xData.add(10.0);
        yData.add(currentPrice);
    }

    private static void showChartDialog(String mineralName, double currentPrice,
                                        OHLCChart chart, Runnable onBuyCallback,
                                        List<PriceHistory.PricePoint> priceHistory) {
        showChartDialogWithChart(mineralName, currentPrice, chart, onBuyCallback, priceHistory);
    }

    private static void showChartDialog(String mineralName, double currentPrice,
                                        XYChart chart, Runnable onBuyCallback,
                                        List<PriceHistory.PricePoint> priceHistory) {
        showChartDialogWithChart(mineralName, currentPrice, chart, onBuyCallback, priceHistory);
    }

    private static void showChartDialogWithChart(String mineralName, double currentPrice,
                                                 Object chart, Runnable onBuyCallback,
                                                 List<PriceHistory.PricePoint> priceHistory) {
        stopTimer();

        JPanel chartPanel;
        if (chart instanceof OHLCChart) {
            chartPanel = new XChartPanel<>((OHLCChart) chart);
        } else if (chart instanceof XYChart) {
            chartPanel = new XChartPanel<>((XYChart) chart);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error al crear el gráfico para " + mineralName,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentChartPanel = chartPanel;

        JButton buyButton = new JButton("🛒 Comprar a $" + String.format("%.2f", currentPrice));
        JButton cancelButton = new JButton("❌ Cancelar");
        JButton refreshButton = new JButton("🔄 Actualizar Gráfico");

        buyButton.setBackground(new Color(40, 167, 69));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFont(new Font("Inter", Font.BOLD, 14));

        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Inter", Font.BOLD, 14));

        refreshButton.setBackground(new Color(23, 162, 184));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Inter", Font.BOLD, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(buyButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("Análisis de Trading - " + mineralName + " (Actualizado: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
        dialog.setModal(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        currentDialog = dialog;

        setupAutoUpdate(mineralName, dialog, mainPanel, buyButton);

        buyButton.addActionListener(e -> {
            stopTimer();
            onBuyCallback.run();
            dialog.dispose();
        });

        refreshButton.addActionListener(e -> {
            refreshChart(mineralName, dialog, mainPanel, buyButton);
        });

        cancelButton.addActionListener(e -> {
            stopTimer();
            dialog.dispose();
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopTimer();
            }
        });

        dialog.setVisible(true);
    }

    private static void setupAutoUpdate(String mineralName, JDialog dialog, JPanel mainPanel, JButton buyButton) {
        updateTimer = new javax.swing.Timer(3000, e -> {
            refreshChart(mineralName, dialog, mainPanel, buyButton);
        });
        updateTimer.start();
    }

    private static void refreshChart(String mineralName, JDialog dialog, JPanel mainPanel, JButton buyButton) {
        try {
            System.out.println("Actualizando gráfico para: " + mineralName);

            Economy economy = Economy.getInstance();
            double currentPrice = economy.getBuyPrice(mineralName);
            List<PriceHistory.PricePoint> currentHistory = economy.getPriceHistory(mineralName);

            System.out.println("Precio actual: " + currentPrice + ", Historial: " + currentHistory.size() + " puntos");

            buyButton.setText("🛒 Comprar a $" + String.format("%.2f", currentPrice));

            dialog.setTitle("Análisis de Trading - " + mineralName + " (Actualizado: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");

            Object newChart = createUpdatedChart(mineralName, currentPrice, currentHistory);
            if (newChart != null) {
                JPanel newChartPanel;
                if (newChart instanceof OHLCChart) {
                    newChartPanel = new XChartPanel<>((OHLCChart) newChart);
                } else {
                    newChartPanel = new XChartPanel<>((XYChart) newChart);
                }

                mainPanel.remove(currentChartPanel);
                mainPanel.add(newChartPanel, BorderLayout.CENTER);
                currentChartPanel = newChartPanel;

                mainPanel.revalidate();
                mainPanel.repaint();
                dialog.pack();

                System.out.println("Gráfico actualizado exitosamente");
            }

        } catch (Exception ex) {
            System.err.println("Error actualizando gráfico: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static Object createUpdatedChart(String mineralName, double currentPrice, List<PriceHistory.PricePoint> priceHistory) {
        System.out.println("Creando gráfico actualizado con " + priceHistory.size() + " puntos de datos");

        if (priceHistory.size() >= 3) {
            try {
                List<Double> timeData = new ArrayList<>();
                List<Double> openData = new ArrayList<>();
                List<Double> highData = new ArrayList<>();
                List<Double> lowData = new ArrayList<>();
                List<Double> closeData = new ArrayList<>();

                createCandleData(priceHistory, timeData, openData, highData, lowData, closeData);

                System.out.println("Datos de velas - Tiempo: " + timeData.size() + ", Apertura: " + openData.size());

                OHLCChart chart = new OHLCChartBuilder()
                        .width(800)
                        .height(500)
                        .title("Gráfico de Velas - " + mineralName + " (Precio Actual: $" + String.format("%.2f", currentPrice) + ")")
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

        System.out.println("Usando gráfico de línea como fallback");
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(500)
                .title("Tendencia de Precio - " + mineralName + " (Precio Actual: $" + String.format("%.2f", currentPrice) + ")")
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

    private static void stopTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
            System.out.println("Timer detenido");
        }
    }

    private static void createCandleData(List<PriceHistory.PricePoint> priceHistory,
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

        if (openData.isEmpty() && !priceHistory.isEmpty()) {
            double price = priceHistory.get(0).getPrice();
            timeData.add(0.0);
            openData.add(price);
            highData.add(price * 1.05);
            lowData.add(price * 0.95);
            closeData.add(price);
        }
    }
}