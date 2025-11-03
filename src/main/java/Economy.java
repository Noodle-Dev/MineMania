import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import javax.swing.Timer;
import java.util.List;
import java.util.ArrayList;

public class Economy {
    private static Economy instance;
    private double money;
    private HashMap<String, Double> buyPrices;
    private HashMap<String, Double> sellPrices;
    private Random random;
    private Timer priceTimer;

    private List<String> mineralNames;

    private Economy() {
        this.money = 100.0;
        this.random = new Random();
        initializePrices();
        startPriceUpdates();
    }

    public static Economy getInstance() {
        if (instance == null) {
            instance = new Economy();
        }
        return instance;
    }

    public List<String> getMineralNames() {
        return mineralNames;
    }

    private void initializePrices() {
        buyPrices = new HashMap<>();
        sellPrices = new HashMap<>();
        mineralNames = new ArrayList<>();

        loadMineralData(); // Carga los precios iniciales

        // Calcula los precios de venta iniciales
        for (String mineral : buyPrices.keySet()) {
            sellPrices.put(mineral, buyPrices.get(mineral) * 0.7);
        }
    }

    private void loadMineralData() {
        // Lógica de carga de JSON eliminada por simplicidad,
        // ya que no estaba implementada.
        // Se usan precios predeterminados directamente.

        buyPrices.put("💎 Diamante", 50.0);
        buyPrices.put("⛏️ Carbón", 5.0);
        buyPrices.put("🧪 Uranio", 150.0);
        buyPrices.put("💰 Oro", 80.0);

        mineralNames.add("💎 Diamante");
        mineralNames.add("⛏️ Carbón");
        mineralNames.add("🧪 Uranio");
        mineralNames.add("💰 Oro");
    }

    private void startPriceUpdates() {
        priceTimer = new Timer(5000, e -> updatePrices());
        priceTimer.start();
    }

    /**
     * Compra un mineral, descontando el dinero.
     * (Antes llamado buyPlant)
     */
    public boolean buyMineral(String mineralName) {
        double price = buyPrices.get(mineralName);
        if (money >= price) {
            money -= price;
            return true;
        }
        return false;
    }

    /**
     * Vende un mineral, aumentando el dinero.
     * (Antes llamado sellPlant)
     */
    public void sellMineral(String mineralName) {
        double price = sellPrices.get(mineralName);
        money += price;
    }

    public double getMoney() {
        return money;
    }

    public double getBuyPrice(String mineralName) {
        return buyPrices.get(mineralName);
    }

    public double getSellPrice(String mineralName) {
        return sellPrices.get(mineralName);
    }

    private void updatePrices() {
        // Actualización regular de precios sin AI
        for (String mineral : new ArrayList<>(buyPrices.keySet())) {
            double currentBuyPrice = buyPrices.get(mineral);
            // Fluctuación entre -20% y +20%
            double fluctuation = (random.nextDouble() * 0.4) - 0.2;
            double newBuyPrice = currentBuyPrice * (1 + fluctuation);

            // Asegura que los precios se mantengan en un rango razonable
            newBuyPrice = Math.max(5.0, Math.min(300.0, newBuyPrice));

            // Redondea a 2 decimales
            buyPrices.put(mineral, Math.round(newBuyPrice * 100.0) / 100.0);

            // Actualiza el precio de venta basado en el nuevo precio de compra
            double newSellPrice = newBuyPrice * 0.7;
            sellPrices.put(mineral, Math.round(newSellPrice * 100.0) / 100.0);
        }
    }

    public void forcePriceUpdate() {
        updatePrices();
    }

    public void reset() {
        money = 100.0;
        initializePrices();
    }
}