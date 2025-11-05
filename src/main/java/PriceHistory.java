import java.util.*;

public class PriceHistory {
    private Map<String, List<PricePoint>> history;

    public PriceHistory() {
        this.history = new HashMap<>();
    }

    public void addPricePoint(String mineralName, double price, long timestamp) {
        history.putIfAbsent(mineralName, new ArrayList<>());
        List<PricePoint> mineralHistory = history.get(mineralName);
        mineralHistory.add(new PricePoint(price, timestamp));

        // Mantener solo los últimos 30 puntos para el gráfico (más manejable)
        if (mineralHistory.size() > 30) {
            mineralHistory.remove(0);
        }
    }

    public List<PricePoint> getPriceHistory(String mineralName) {
        List<PricePoint> historyList = history.getOrDefault(mineralName, new ArrayList<>());

        // Si no hay historial, crear uno mínimo
        if (historyList.isEmpty()) {
            // Crear un punto de datos básico para evitar errores
            historyList.add(new PricePoint(50.0, System.currentTimeMillis()));
            history.put(mineralName, historyList);
        }

        return historyList;
    }

    public static class PricePoint {
        private double price;
        private long timestamp;

        public PricePoint(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        public double getPrice() { return price; }
        public long getTimestamp() { return timestamp; }
    }
}